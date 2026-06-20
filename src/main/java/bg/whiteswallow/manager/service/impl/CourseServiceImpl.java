package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.course.CourseAddDTO;
import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.repository.CourseRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseServiceImpl(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void addCourse(CourseAddDTO courseAddDTO) {
        User instructor = userRepository.findById(courseAddDTO.getInstructorId()).orElseThrow();

        Course course = Course.builder()
                .name(courseAddDTO.getName())
                .type(courseAddDTO.getType())
                .pricePerLesson(courseAddDTO.getPricePerLesson())
                .instructor(instructor) // Закачаме преподавателя
                .build();
        courseRepository.save(course);
    }

    @Override
    public void updateCourse(UUID id, CourseAddDTO courseDTO) {
        Course course = courseRepository.findById(id).orElseThrow();
        User instructor = userRepository.findById(courseDTO.getInstructorId()).orElseThrow();

        course.setName(courseDTO.getName());
        course.setType(courseDTO.getType());
        course.setPricePerLesson(courseDTO.getPricePerLesson());
        course.setInstructor(instructor);
        courseRepository.save(course);
    }

    @Override
    public CourseAddDTO getCourseForEdit(UUID id) {
        Course course = courseRepository.findById(id).orElseThrow();
        CourseAddDTO dto = new CourseAddDTO();
        dto.setName(course.getName());
        dto.setType(course.getType());
        dto.setPricePerLesson(course.getPricePerLesson());
        dto.setInstructorId(course.getInstructor().getId());
        return dto;
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public void deleteCourse(UUID id) {
        courseRepository.deleteById(id);
    }

    @Override
    public List<Course> getCoursesByInstructor(UUID instructorId) {
        return courseRepository.findAllByInstructorId(instructorId);
    }

    @Override
    public Course getCourseById(UUID id) {
        return courseRepository.findById(id).orElseThrow();
    }

}