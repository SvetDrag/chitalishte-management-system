package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.entity.course.LessonAttendance;
import bg.whiteswallow.manager.model.entity.course.LessonSlot;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.exception.ResourceNotFoundException;
import bg.whiteswallow.manager.repository.LessonAttendanceRepository;
import bg.whiteswallow.manager.repository.LessonSlotRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.LessonAttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class LessonAttendanceServiceImpl implements LessonAttendanceService {

    private final LessonAttendanceRepository attendanceRepository;
    private final LessonSlotRepository slotRepository;
    private final UserRepository userRepository;

    public LessonAttendanceServiceImpl(LessonAttendanceRepository attendanceRepository, LessonSlotRepository slotRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void markAttendance(UUID userId, UUID slotId, boolean isPaid) {

        if (attendanceRepository.existsByUserIdAndLessonSlotId(userId, slotId)) {
            return;
        }

        LessonSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Часът не е намерен."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен."));

        LessonAttendance attendance = LessonAttendance.builder()
                .user(user)
                .course(slot.getCourse())
                .lessonSlot(slot)
                .date(LocalDate.now())
                .isPaid(isPaid)
                .build();

        attendanceRepository.save(attendance);

    }

    @Override
    public void removeAttendance(UUID attendanceId) {
        attendanceRepository.deleteById(attendanceId);
    }

    @Override
    public void togglePayment(UUID attendanceId) {
        LessonAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Записът за присъствие не е намерен."));

            attendance.setPaid(!attendance.isPaid());
        attendanceRepository.save(attendance);
    }

    @Override
    public List<LessonAttendance> getAllAttendances() {
        return attendanceRepository.findAll();
    }

    @Override
    public List<LessonAttendance> getAttendancesForSlot(UUID slotId) {
        return attendanceRepository.findAllByLessonSlotId(slotId);
    }

    @Override
    public List<User> getPendingUsersForSlot(UUID slotId) {
        LessonSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Часът не е намерен."));

        List<UUID> reportedUserIds = attendanceRepository.findAllByLessonSlotId(slotId).stream()
                .map(a -> a.getUser().getId())
                .toList();

        return slot.getEnrolledUsers().stream()
                .filter(u -> !reportedUserIds.contains(u.getId()))
                .toList();
    }
}