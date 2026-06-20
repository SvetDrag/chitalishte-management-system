package bg.whiteswallow.manager.repository;

import bg.whiteswallow.manager.model.entity.course.LessonSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LessonSlotRepository extends JpaRepository<LessonSlot, UUID> {

    List<LessonSlot> findAllByCourseInstructorIdOrderByStartTimeAsc(UUID instructorId);

    List<LessonSlot> findAllByCourseIdOrderByStartTimeAsc(UUID courseId);
    List<LessonSlot> findAllByStartTimeAfterOrderByStartTimeAsc(LocalDateTime time);
    List<LessonSlot> findAllByCourseIdAndStartTimeAfterOrderByStartTimeAsc(UUID courseId, java.time.LocalDateTime time);

}