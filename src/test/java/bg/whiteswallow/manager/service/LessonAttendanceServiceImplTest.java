package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.exception.ResourceNotFoundException;
import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.course.LessonAttendance;
import bg.whiteswallow.manager.model.entity.course.LessonSlot;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.repository.LessonAttendanceRepository;
import bg.whiteswallow.manager.repository.LessonSlotRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.impl.LessonAttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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
class LessonAttendanceServiceImplTest {

    @Mock
    private LessonAttendanceRepository attendanceRepository;

    @Mock
    private LessonSlotRepository slotRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LessonAttendanceServiceImpl attendanceService;

    private User student;
    private LessonSlot slot;

    @BeforeEach
    void setUp() {
        student = User.builder().id(UUID.randomUUID()).username("student").build();
        Course course = Course.builder().id(UUID.randomUUID()).name("Школа").build();
        slot = LessonSlot.builder().id(UUID.randomUUID()).course(course).enrolledUsers(new ArrayList<>(List.of(student))).build();
    }

    @Test
    void markAttendance_savesAttendance_whenNotAlreadyRecorded() {
        when(attendanceRepository.existsByUserIdAndLessonSlotId(student.getId(), slot.getId())).thenReturn(false);
        when(slotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        attendanceService.markAttendance(student.getId(), slot.getId(), true);

        verify(attendanceRepository).save(any(LessonAttendance.class));
    }

    @Test
    void markAttendance_doesNothing_whenAlreadyRecorded() {
        when(attendanceRepository.existsByUserIdAndLessonSlotId(student.getId(), slot.getId())).thenReturn(true);

        attendanceService.markAttendance(student.getId(), slot.getId(), true);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void markAttendance_throwsResourceNotFound_whenSlotMissing() {
        when(attendanceRepository.existsByUserIdAndLessonSlotId(student.getId(), slot.getId())).thenReturn(false);
        when(slotRepository.findById(slot.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.markAttendance(student.getId(), slot.getId(), false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeAttendance_delegatesToRepository() {
        UUID attendanceId = UUID.randomUUID();

        attendanceService.removeAttendance(attendanceId);

        verify(attendanceRepository).deleteById(attendanceId);
    }

    @Test
    void togglePayment_flipsPaidFlag() {
        UUID attendanceId = UUID.randomUUID();
        LessonAttendance attendance = LessonAttendance.builder().id(attendanceId).user(student).isPaid(false).build();
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));

        attendanceService.togglePayment(attendanceId);

        assertThat(attendance.isPaid()).isTrue();
        verify(attendanceRepository).save(attendance);
    }

    @Test
    void togglePayment_throwsResourceNotFound_whenMissing() {
        UUID attendanceId = UUID.randomUUID();
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.togglePayment(attendanceId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllAttendances_returnsRepositoryResult() {
        List<LessonAttendance> attendances = List.of(LessonAttendance.builder().user(student).build());
        when(attendanceRepository.findAll()).thenReturn(attendances);

        assertThat(attendanceService.getAllAttendances()).isEqualTo(attendances);
    }

    @Test
    void getPendingUsersForSlot_excludesAlreadyReportedUsers() {
        User reportedUser = User.builder().id(UUID.randomUUID()).username("reported").build();
        slot.getEnrolledUsers().add(reportedUser);
        LessonAttendance existing = LessonAttendance.builder().user(reportedUser).build();

        when(slotRepository.findById(slot.getId())).thenReturn(Optional.of(slot));
        when(attendanceRepository.findAllByLessonSlotId(slot.getId())).thenReturn(List.of(existing));

        List<User> pending = attendanceService.getPendingUsersForSlot(slot.getId());

        assertThat(pending).containsExactly(student);
    }
}
