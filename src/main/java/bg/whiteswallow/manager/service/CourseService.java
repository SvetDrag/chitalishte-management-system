package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.dto.course.CourseAddDTO;
import bg.whiteswallow.manager.model.entity.course.Course;

import java.util.List;
import java.util.UUID;

public interface CourseService {

    void addCourse(CourseAddDTO courseAddDTO);
    List<Course> getAllCourses();
    void deleteCourse(UUID id);
    CourseAddDTO getCourseForEdit(UUID id);
    void updateCourse(UUID id, CourseAddDTO courseAddDTO);
}
