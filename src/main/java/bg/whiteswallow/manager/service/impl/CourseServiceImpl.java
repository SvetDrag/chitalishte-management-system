package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.course.CourseAddDTO;
import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.exception.ResourceNotFoundException;
import bg.whiteswallow.manager.repository.CourseRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseServiceImpl.class);

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseServiceImpl(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Override
    @CacheEvict(value = "courses", allEntries = true)
    public void addCourse(CourseAddDTO courseAddDTO) {
        validateAtLeastOnePrice(courseAddDTO);
        User instructor = userRepository.findById(courseAddDTO.getInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException("Преподавателят не е намерен."));

        Course course = Course.builder()
                .name(courseAddDTO.getName())
                .groupPricePerLesson(courseAddDTO.getGroupPricePerLesson())
                .individualPricePerLesson(courseAddDTO.getIndividualPricePerLesson())
                .instructor(instructor)
                .build();
        courseRepository.save(course);
        log.info("Created course '{}'", course.getName());
    }

    @Override
    @CacheEvict(value = "courses", allEntries = true)
    public void updateCourse(UUID id, CourseAddDTO courseDTO) {
        validateAtLeastOnePrice(courseDTO);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Школата не е намерена."));
        User instructor = userRepository.findById(courseDTO.getInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException("Преподавателят не е намерен."));

        course.setName(courseDTO.getName());
        course.setGroupPricePerLesson(courseDTO.getGroupPricePerLesson());
        course.setIndividualPricePerLesson(courseDTO.getIndividualPricePerLesson());
        course.setInstructor(instructor);
        courseRepository.save(course);
        log.info("Updated course {}", id);
    }

    @Override
    public CourseAddDTO getCourseForEdit(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Школата не е намерена."));
        CourseAddDTO dto = new CourseAddDTO();
        dto.setName(course.getName());
        dto.setGroupPricePerLesson(course.getGroupPricePerLesson());
        dto.setIndividualPricePerLesson(course.getIndividualPricePerLesson());
        dto.setInstructorId(course.getInstructor().getId());
        return dto;
    }

    @Override
    @Cacheable("courses")
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    @CacheEvict(value = "courses", allEntries = true)
    public void deleteCourse(UUID id) {
        courseRepository.deleteById(id);
        log.info("Deleted course {}", id);
    }

    @Override
    public List<Course> getCoursesByInstructor(UUID instructorId) {
        return courseRepository.findAllByInstructorId(instructorId);
    }

    @Override
    public Course getCourseById(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Школата не е намерена."));
    }

    private void validateAtLeastOnePrice(CourseAddDTO courseDTO) {
        if (courseDTO.getGroupPricePerLesson() == null && courseDTO.getIndividualPricePerLesson() == null) {
            throw new IllegalArgumentException("Школата трябва да предлага поне един формат - групов или индивидуален.");
        }
    }
}
