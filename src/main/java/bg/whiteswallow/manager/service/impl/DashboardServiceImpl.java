package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.dashboard.AdminDashboardDTO;
import bg.whiteswallow.manager.model.dto.dashboard.InstructorDashboardDTO;
import bg.whiteswallow.manager.model.dto.dashboard.PublicDashboardDTO;
import bg.whiteswallow.manager.model.entity.course.LessonAttendance;
import bg.whiteswallow.manager.model.entity.course.LessonSlot;
import bg.whiteswallow.manager.model.entity.event.Event;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.manager.rental.dto.RentalStatus;
import bg.whiteswallow.manager.rental.service.RentalIntegrationService;
import bg.whiteswallow.manager.repository.CourseRepository;
import bg.whiteswallow.manager.repository.EventRepository;
import bg.whiteswallow.manager.repository.LessonSlotRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.DashboardService;
import bg.whiteswallow.manager.service.LessonAttendanceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int UPCOMING_EVENTS_PREVIEW_LIMIT = 3;

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EventRepository eventRepository;
    private final LessonSlotRepository lessonSlotRepository;
    private final LessonAttendanceService lessonAttendanceService;
    private final RentalIntegrationService rentalIntegrationService;

    public DashboardServiceImpl(UserRepository userRepository, CourseRepository courseRepository,
                                 EventRepository eventRepository, LessonSlotRepository lessonSlotRepository,
                                 LessonAttendanceService lessonAttendanceService,
                                 RentalIntegrationService rentalIntegrationService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.eventRepository = eventRepository;
        this.lessonSlotRepository = lessonSlotRepository;
        this.lessonAttendanceService = lessonAttendanceService;
        this.rentalIntegrationService = rentalIntegrationService;
    }

    @Override
    public PublicDashboardDTO getPublicDashboard() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> upcomingEvents = eventRepository.findAll().stream()
                .filter(event -> event.getEventDate().isAfter(now))
                .sorted(Comparator.comparing(Event::getEventDate))
                .limit(UPCOMING_EVENTS_PREVIEW_LIMIT)
                .toList();

        return PublicDashboardDTO.builder()
                .upcomingEvents(upcomingEvents)
                .activeCourses(courseRepository.findAll())
                .build();
    }

    @Override
    public AdminDashboardDTO getAdminDashboard() {
        LocalDateTime now = LocalDateTime.now();
        long upcomingEventsCount = eventRepository.findAll().stream()
                .filter(event -> event.getEventDate().isAfter(now))
                .count();

        return AdminDashboardDTO.builder()
                .totalUsers(userRepository.count())
                .totalCourses(courseRepository.count())
                .upcomingEventsCount(upcomingEventsCount)
                .totalRevenue(calculateTotalRevenue())
                .build();
    }

    @Override
    public InstructorDashboardDTO getInstructorDashboard(UUID instructorId) {
        LocalDateTime now = LocalDateTime.now();
        List<LessonSlot> mySlots = lessonSlotRepository.findAllByCourseInstructorIdOrderByStartTimeAsc(instructorId);

        long upcomingSlotsCount = mySlots.stream()
                .filter(slot -> slot.getStartTime().isAfter(now))
                .count();

        Set<UUID> distinctStudents = mySlots.stream()
                .flatMap(slot -> slot.getEnrolledUsers().stream())
                .map(User::getId)
                .collect(Collectors.toSet());

        BigDecimal pendingAmount = lessonAttendanceService.getAllAttendances().stream()
                .filter(attendance -> attendance.getCourse().getInstructor().getId().equals(instructorId))
                .filter(attendance -> !attendance.isPaid())
                .map(attendance -> attendance.getLessonSlot().getEffectivePrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return InstructorDashboardDTO.builder()
                .myCoursesCount(courseRepository.findAllByInstructorId(instructorId).size())
                .upcomingSlotsCount(upcomingSlotsCount)
                .myStudentsCount(distinctStudents.size())
                .pendingAmount(pendingAmount)
                .build();
    }

    private BigDecimal calculateTotalRevenue() {
        BigDecimal fromCourses = lessonAttendanceService.getAllAttendances().stream()
                .filter(LessonAttendance::isPaid)
                .map(attendance -> attendance.getLessonSlot().getEffectivePrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RentalRequestResponseDTO> rentals = rentalIntegrationService.getAllRentalRequests();
        BigDecimal fromRentals = rentals.stream()
                .filter(rental -> rental.getStatus() == RentalStatus.CONFIRMED || rental.getStatus() == RentalStatus.COMPLETED)
                .map(RentalRequestResponseDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return fromCourses.add(fromRentals);
    }
}
