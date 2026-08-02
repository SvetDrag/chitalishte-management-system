package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.exception.ResourceNotFoundException;
import bg.whiteswallow.manager.model.dto.course.CourseAddDTO;
import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.repository.CourseRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private CourseAddDTO courseAddDTO;
    private User instructor;

    @BeforeEach
    void setUp() {
        instructor = User.builder().id(UUID.randomUUID()).username("teacher").build();
        courseAddDTO = new CourseAddDTO();
        courseAddDTO.setName("Школа по пиано");
        courseAddDTO.setGroupPricePerLesson(new BigDecimal("10"));
        courseAddDTO.setInstructorId(instructor.getId());
    }

    @Test
    void addCourse_throwsIllegalArgument_whenNoPriceProvided() {
        courseAddDTO.setGroupPricePerLesson(null);
        courseAddDTO.setIndividualPricePerLesson(null);

        assertThatThrownBy(() -> courseService.addCourse(courseAddDTO))
                .isInstanceOf(IllegalArgumentException.class);
        verify(courseRepository, never()).save(any());
    }

    @Test
    void addCourse_savesCourse_whenInstructorFoundAndPriceProvided() {
        when(userRepository.findById(instructor.getId())).thenReturn(Optional.of(instructor));

        courseService.addCourse(courseAddDTO);

        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void addCourse_throwsResourceNotFound_whenInstructorMissing() {
        when(userRepository.findById(instructor.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.addCourse(courseAddDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCourse_updatesFields_whenFound() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().id(courseId).name("Old").instructor(instructor).build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(instructor.getId())).thenReturn(Optional.of(instructor));

        courseService.updateCourse(courseId, courseAddDTO);

        assertThat(course.getName()).isEqualTo("Школа по пиано");
        verify(courseRepository).save(course);
    }

    @Test
    void updateCourse_throwsResourceNotFound_whenCourseMissing() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.updateCourse(courseId, courseAddDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCourseForEdit_mapsEntityToDto() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().id(courseId).name("Школа").groupPricePerLesson(new BigDecimal("15")).instructor(instructor).build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        CourseAddDTO result = courseService.getCourseForEdit(courseId);

        assertThat(result.getName()).isEqualTo("Школа");
        assertThat(result.getInstructorId()).isEqualTo(instructor.getId());
    }

    @Test
    void getCourseForEdit_throwsResourceNotFound_whenMissing() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseForEdit(courseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllCourses_returnsRepositoryResult() {
        List<Course> courses = List.of(Course.builder().name("A").build());
        when(courseRepository.findAll()).thenReturn(courses);

        assertThat(courseService.getAllCourses()).isEqualTo(courses);
    }

    @Test
    void deleteCourse_delegatesToRepository() {
        UUID courseId = UUID.randomUUID();

        courseService.deleteCourse(courseId);

        verify(courseRepository).deleteById(courseId);
    }

    @Test
    void getCoursesByInstructor_delegatesToRepository() {
        List<Course> courses = List.of(Course.builder().name("A").instructor(instructor).build());
        when(courseRepository.findAllByInstructorId(instructor.getId())).thenReturn(courses);

        assertThat(courseService.getCoursesByInstructor(instructor.getId())).isEqualTo(courses);
    }

    @Test
    void getCourseById_returnsCourse_whenFound() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder().id(courseId).name("Школа").build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        assertThat(courseService.getCourseById(courseId)).isEqualTo(course);
    }

    @Test
    void getCourseById_throwsResourceNotFound_whenMissing() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(courseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
