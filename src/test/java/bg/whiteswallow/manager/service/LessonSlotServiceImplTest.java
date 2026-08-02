package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.exception.ResourceNotFoundException;
import bg.whiteswallow.manager.model.dto.course.LessonSlotAddDTO;
import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.course.CourseType;
import bg.whiteswallow.manager.model.entity.course.LessonSlot;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.repository.CourseRepository;
import bg.whiteswallow.manager.repository.LessonSlotRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.impl.LessonSlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonSlotServiceImplTest {

    @Mock
    private LessonSlotRepository lessonSlotRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LessonSlotServiceImpl lessonSlotService;

    private User instructor;
    private Course course;
    private User student;

    @BeforeEach
    void setUp() {
        instructor = User.builder().id(UUID.randomUUID()).username("instructor").build();
        course = Course.builder().id(UUID.randomUUID()).name("Школа").instructor(instructor)
                .groupPricePerLesson(new BigDecimal("10")).build();
        student = User.builder().id(UUID.randomUUID()).username("student").build();
    }

    @Test
    void addSlot_savesSlot_whenInstructorOwnsCourseAndFormatOffered() {
        LessonSlotAddDTO dto = new LessonSlotAddDTO();
        dto.setCourseId(course.getId());
        dto.setType(CourseType.GROUP);
        dto.setStartTime(LocalDateTime.now().plusDays(1));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        lessonSlotService.addSlot(dto, instructor.getId());

        verify(lessonSlotRepository).save(any(LessonSlot.class));
    }

    @Test
    void addSlot_throwsIllegalArgument_whenInstructorDoesNotOwnCourse() {
        LessonSlotAddDTO dto = new LessonSlotAddDTO();
        dto.setCourseId(course.getId());
        dto.setType(CourseType.GROUP);
        dto.setStartTime(LocalDateTime.now().plusDays(1));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> lessonSlotService.addSlot(dto, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addSlot_throwsIllegalArgument_whenFormatNotOffered() {
        LessonSlotAddDTO dto = new LessonSlotAddDTO();
        dto.setCourseId(course.getId());
        dto.setType(CourseType.INDIVIDUAL);
        dto.setStartTime(LocalDateTime.now().plusDays(1));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> lessonSlotService.addSlot(dto, instructor.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addSlot_throwsResourceNotFound_whenCourseMissing() {
        LessonSlotAddDTO dto = new LessonSlotAddDTO();
        dto.setCourseId(UUID.randomUUID());
        dto.setType(CourseType.GROUP);
        when(courseRepository.findById(dto.getCourseId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonSlotService.addSlot(dto, instructor.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void enrollUser_addsUser_whenSlotHasCapacity() {
        LessonSlot slot = LessonSlot.builder().id(UUID.randomUUID()).course(course).maxCapacity(2)
                .enrolledUsers(new ArrayList<>()).build();
        when(lessonSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        boolean result = lessonSlotService.enrollUser(slot.getId(), student.getId());

        assertThat(result).isTrue();
        assertThat(slot.getEnrolledUsers()).contains(student);
    }

    @Test
    void enrollUser_returnsFalse_whenAlreadyEnrolled() {
        LessonSlot slot = LessonSlot.builder().id(UUID.randomUUID()).course(course).maxCapacity(2)
                .enrolledUsers(new ArrayList<>(java.util.List.of(student))).build();
        when(lessonSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        boolean result = lessonSlotService.enrollUser(slot.getId(), student.getId());

        assertThat(result).isFalse();
    }

    @Test
    void enrollUser_returnsFalse_whenSlotFull() {
        LessonSlot slot = LessonSlot.builder().id(UUID.randomUUID()).course(course).maxCapacity(1)
                .enrolledUsers(new ArrayList<>(java.util.List.of(User.builder().id(UUID.randomUUID()).build()))).build();
        when(lessonSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        boolean result = lessonSlotService.enrollUser(slot.getId(), student.getId());

        assertThat(result).isFalse();
    }

    @Test
    void unenrollUser_removesUser_whenEnrolled() {
        LessonSlot slot = LessonSlot.builder().id(UUID.randomUUID()).course(course).maxCapacity(2)
                .enrolledUsers(new ArrayList<>(java.util.List.of(student))).build();
        when(lessonSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        boolean result = lessonSlotService.unenrollUser(slot.getId(), student.getId());

        assertThat(result).isTrue();
        assertThat(slot.getEnrolledUsers()).doesNotContain(student);
    }

    @Test
    void unenrollUser_returnsFalse_whenNotEnrolled() {
        LessonSlot slot = LessonSlot.builder().id(UUID.randomUUID()).course(course).maxCapacity(2)
                .enrolledUsers(new ArrayList<>()).build();
        when(lessonSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        boolean result = lessonSlotService.unenrollUser(slot.getId(), student.getId());

        assertThat(result).isFalse();
    }

    @Test
    void getSlotById_returnsSlot_whenFound() {
        LessonSlot slot = LessonSlot.builder().id(UUID.randomUUID()).course(course).build();
        when(lessonSlotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));

        assertThat(lessonSlotService.getSlotById(slot.getId())).isEqualTo(slot);
    }

    @Test
    void getSlotById_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(lessonSlotRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonSlotService.getSlotById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
