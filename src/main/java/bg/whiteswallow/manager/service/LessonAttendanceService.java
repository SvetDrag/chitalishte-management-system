package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.entity.course.LessonAttendance;

import java.util.List;
import java.util.UUID;

public interface LessonAttendanceService {
    void markAttendance(UUID userId, UUID slotId, boolean isPaid);
    void removeAttendance(UUID attendanceId);
    void togglePayment(UUID attendanceId);
    List<LessonAttendance> getAllAttendances();
}