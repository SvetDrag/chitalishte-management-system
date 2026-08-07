package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.dto.dashboard.AdminDashboardDTO;
import bg.whiteswallow.manager.model.dto.dashboard.InstructorDashboardDTO;
import bg.whiteswallow.manager.model.dto.dashboard.PublicDashboardDTO;
import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.course.LessonAttendance;
import bg.whiteswallow.manager.model.entity.course.LessonSlot;
import bg.whiteswallow.manager.model.entity.event.Event;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.rental.dto.HallResponseDTO;
import bg.whiteswallow.manager.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.manager.rental.dto.RentalStatus;
import bg.whiteswallow.manager.rental.service.RentalIntegrationService;
import bg.whiteswallow.manager.repository.CourseRepository;
import bg.whiteswallow.manager.repository.EventRepository;
import bg.whiteswallow.manager.repository.LessonSlotRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private LessonSlotRepository lessonSlotRepository;
    @Mock
    private LessonAttendanceService lessonAttendanceService;
    @Mock
    private RentalIntegrationService rentalIntegrationService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private Event event(String title, LocalDateTime date) {
        return Event.builder().id(UUID.randomUUID()).title(title).eventDate(date).location("Зала").build();
    }

    private RentalRequestResponseDTO rental(RentalStatus status, BigDecimal price) {
        RentalRequestResponseDTO dto = new RentalRequestResponseDTO();
        dto.setId(UUID.randomUUID());
        dto.setStatus(status);
        dto.setPrice(price);
        dto.setHall(new HallResponseDTO());
        return dto;
    }

    @Test
    void getPublicDashboard_returnsOnlyFutureEventsSortedAndLimited() {
        LocalDateTime now = LocalDateTime.now();
        Event past = event("Минало събитие", now.minusDays(1));
        Event soon = event("Скоро", now.plusDays(1));
        Event later = event("По-късно", now.plusDays(2));
        Event evenLater = event("Най-късно", now.plusDays(3));
        Event lastOne = event("Извън лимита", now.plusDays(4));
        when(eventRepository.findAll()).thenReturn(List.of(lastOne, past, later, soon, evenLater));
        List<Course> courses = List.of(Course.builder().name("Пиано").build());
        when(courseRepository.findAll()).thenReturn(courses);

        PublicDashboardDTO result = dashboardService.getPublicDashboard();

        assertThat(result.getUpcomingEvents()).extracting(Event::getTitle)
                .containsExactly("Скоро", "По-късно", "Най-късно");
        assertThat(result.getActiveCourses()).isEqualTo(courses);
    }

    @Test
    void getAdminDashboard_aggregatesCountsAndRevenue() {
        LocalDateTime now = LocalDateTime.now();
        when(userRepository.count()).thenReturn(11L);
        when(courseRepository.count()).thenReturn(4L);
        when(eventRepository.findAll()).thenReturn(List.of(event("Утре", now.plusDays(1)), event("Вчера", now.minusDays(1))));

        Course course = Course.builder().groupPricePerLesson(new BigDecimal("10")).build();
        LessonSlot slot = LessonSlot.builder().course(course).build();
        LessonAttendance paid = LessonAttendance.builder().lessonSlot(slot).isPaid(true).build();
        LessonAttendance unpaid = LessonAttendance.builder().lessonSlot(slot).isPaid(false).build();
        when(lessonAttendanceService.getAllAttendances()).thenReturn(List.of(paid, unpaid));

        when(rentalIntegrationService.getAllRentalRequests()).thenReturn(List.of(
                rental(RentalStatus.CONFIRMED, new BigDecimal("200")),
                rental(RentalStatus.COMPLETED, new BigDecimal("300")),
                rental(RentalStatus.PENDING, new BigDecimal("999")),
                rental(RentalStatus.CANCELLED, new BigDecimal("999"))
        ));

        AdminDashboardDTO result = dashboardService.getAdminDashboard();

        assertThat(result.getTotalUsers()).isEqualTo(11L);
        assertThat(result.getTotalCourses()).isEqualTo(4L);
        assertThat(result.getUpcomingEventsCount()).isEqualTo(1L);
        assertThat(result.getTotalRevenue()).isEqualByComparingTo("510");
    }

    @Test
    void getInstructorDashboard_countsOwnCoursesUpcomingSlotsStudentsAndPending() {
        UUID instructorId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Course course = Course.builder().id(UUID.randomUUID())
                .instructor(User.builder().id(instructorId).build())
                .groupPricePerLesson(new BigDecimal("15")).build();

        User studentA = User.builder().id(UUID.randomUUID()).build();
        User studentB = User.builder().id(UUID.randomUUID()).build();

        LessonSlot pastSlot = LessonSlot.builder().course(course).startTime(now.minusDays(1))
                .enrolledUsers(List.of(studentA)).build();
        LessonSlot futureSlot = LessonSlot.builder().course(course).startTime(now.plusDays(1))
                .enrolledUsers(List.of(studentA, studentB)).build();

        when(lessonSlotRepository.findAllByCourseInstructorIdOrderByStartTimeAsc(instructorId))
                .thenReturn(List.of(pastSlot, futureSlot));
        when(courseRepository.findAllByInstructorId(instructorId)).thenReturn(List.of(course));

        LessonAttendance unpaidForMe = LessonAttendance.builder().course(course).lessonSlot(pastSlot).isPaid(false).build();
        Course otherCourse = Course.builder().instructor(User.builder().id(UUID.randomUUID()).build())
                .groupPricePerLesson(new BigDecimal("999")).build();
        LessonAttendance unpaidForOther = LessonAttendance.builder().course(otherCourse)
                .lessonSlot(LessonSlot.builder().course(otherCourse).build()).isPaid(false).build();
        when(lessonAttendanceService.getAllAttendances()).thenReturn(List.of(unpaidForMe, unpaidForOther));

        InstructorDashboardDTO result = dashboardService.getInstructorDashboard(instructorId);

        assertThat(result.getMyCoursesCount()).isEqualTo(1);
        assertThat(result.getUpcomingSlotsCount()).isEqualTo(1);
        assertThat(result.getMyStudentsCount()).isEqualTo(2);
        assertThat(result.getPendingAmount()).isEqualByComparingTo("15");
    }
}
