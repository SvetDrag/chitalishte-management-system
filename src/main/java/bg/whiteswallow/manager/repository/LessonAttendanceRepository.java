package bg.whiteswallow.manager.repository;

import bg.whiteswallow.manager.model.entity.course.LessonAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonAttendanceRepository extends JpaRepository<LessonAttendance, UUID> {
    List<LessonAttendance> findAllByUserId(UUID userId);
}