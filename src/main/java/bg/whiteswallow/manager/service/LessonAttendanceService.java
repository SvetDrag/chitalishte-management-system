package bg.whiteswallow.manager.service;

import java.util.UUID;

public interface LessonAttendanceService {
    void markAttendance(UUID userId, UUID slotId, boolean isPaid);
    void removeAttendance(UUID attendanceId);
    void togglePayment(UUID attendanceId);
}