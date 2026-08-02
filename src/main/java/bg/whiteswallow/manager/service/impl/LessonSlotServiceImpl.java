package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.course.LessonSlotAddDTO;
import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.course.CourseType;
import bg.whiteswallow.manager.model.entity.course.LessonSlot;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.exception.ResourceNotFoundException;
import bg.whiteswallow.manager.repository.CourseRepository;
import bg.whiteswallow.manager.repository.LessonSlotRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.LessonSlotService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LessonSlotServiceImpl implements LessonSlotService {

    private final LessonSlotRepository lessonSlotRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public LessonSlotServiceImpl(LessonSlotRepository lessonSlotRepository, CourseRepository courseRepository, UserRepository userRepository) {
        this.lessonSlotRepository = lessonSlotRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<LessonSlot> getAllUpcomingSlots() {
        return lessonSlotRepository.findAllByStartTimeAfterOrderByStartTimeAsc(java.time.LocalDateTime.now());
    }

    @Override
    @Transactional
    public boolean enrollUser(UUID slotId, UUID userId) {
        LessonSlot slot = lessonSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Часът не е намерен."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен."));


        if (slot.getEnrolledUsers().contains(user)) {
            return false;
        }
        if (slot.getEnrolledUsers().size() >= slot.getMaxCapacity()) {
            return false;
        }


        slot.getEnrolledUsers().add(user);
        lessonSlotRepository.save(slot);
        return true;
    }

    @Override
    public void addSlot(LessonSlotAddDTO dto, UUID instructorId) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Школата не е намерена."));

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new IllegalArgumentException("Нямате права над тази школа!");
        }

        if (!course.offers(dto.getType())) {
            throw new IllegalArgumentException("Школата не предлага избрания формат на обучение.");
        }

        int capacity = (dto.getType() == CourseType.INDIVIDUAL) ? 1 : 20;

        LessonSlot slot = LessonSlot.builder()
                .course(course)
                .startTime(dto.getStartTime())
                .type(dto.getType())
                .maxCapacity(capacity)
                .build();

        lessonSlotRepository.save(slot);
    }

    @Override
    public List<LessonSlot> getInstructorSchedule(UUID instructorId) {
        return lessonSlotRepository.findAllByCourseInstructorIdOrderByStartTimeAsc(instructorId);
    }

    @Override
    public List<LessonSlot> getUpcomingSlotsForCourse(UUID courseId) {
        return lessonSlotRepository.findAllByCourseIdAndStartTimeAfterOrderByStartTimeAsc(courseId, java.time.LocalDateTime.now());
    }

    @Override
    @Transactional
    public boolean unenrollUser(UUID slotId, UUID userId) {
        LessonSlot slot = lessonSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Часът не е намерен."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен."));


        if (!slot.getEnrolledUsers().contains(user)) {
            return false;
        }

        slot.getEnrolledUsers().remove(user);
        lessonSlotRepository.save(slot);
        return true;
    }

    @Override
    public List<LessonSlot> getUserUpcomingLessons(UUID userId) {
        return lessonSlotRepository.findAllByEnrolledUsersIdAndStartTimeAfterOrderByStartTimeAsc(userId, java.time.LocalDateTime.now());
    }

    @Override
    public LessonSlot getSlotById(UUID slotId) {
        return lessonSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Часът не е намерен."));
    }
}