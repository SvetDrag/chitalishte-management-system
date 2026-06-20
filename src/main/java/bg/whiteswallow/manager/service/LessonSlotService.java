package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.dto.course.LessonSlotAddDTO;
import bg.whiteswallow.manager.model.entity.course.LessonSlot;

import java.util.List;
import java.util.UUID;

public interface LessonSlotService {
    void addSlot(LessonSlotAddDTO dto, UUID instructorId);
    List<LessonSlot> getInstructorSchedule(UUID instructorId);
    List<LessonSlot> getAllUpcomingSlots();
    boolean enrollUser(UUID slotId, UUID userId);
    List<LessonSlot> getUpcomingSlotsForCourse(UUID courseId);
    boolean unenrollUser(UUID slotId, UUID userId);
    List<LessonSlot> getUserUpcomingLessons(UUID userId);
}