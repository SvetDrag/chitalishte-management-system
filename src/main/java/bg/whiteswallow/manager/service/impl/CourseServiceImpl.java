package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.course.CourseAddDTO;
import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.repository.CourseRepository;
import bg.whiteswallow.manager.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public void addCourse(CourseAddDTO courseAddDTO) {
        Course course = Course.builder()
                .name(courseAddDTO.getName())
                .type(courseAddDTO.getType())
                .pricePerLesson(courseAddDTO.getPricePerLesson())
                .build();
        courseRepository.save(course);
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
    public CourseAddDTO getCourseForEdit(UUID id) {
        Course course = courseRepository.findById(id).orElseThrow();
        CourseAddDTO dto = new CourseAddDTO();
        dto.setName(course.getName());
        dto.setType(course.getType());
        dto.setPricePerLesson(course.getPricePerLesson());
        return dto;
    }

    @Override
    public void updateCourse(UUID id, CourseAddDTO courseDTO) {
        Course course = courseRepository.findById(id).orElseThrow();
        course.setName(courseDTO.getName());
        course.setType(courseDTO.getType());
        course.setPricePerLesson(courseDTO.getPricePerLesson());
        courseRepository.save(course);
    }
}