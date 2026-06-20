package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.course.LessonSlotAddDTO;
import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.course.CourseType;
import bg.whiteswallow.manager.model.entity.course.LessonSlot;
import bg.whiteswallow.manager.model.entity.user.User;
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
        LessonSlot slot = lessonSlotRepository.findById(slotId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();


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
        Course course = courseRepository.findById(dto.getCourseId()).orElseThrow();

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new IllegalArgumentException("Нямате права над тази школа!");
        }


        int capacity = (course.getType() == CourseType.INDIVIDUAL) ? 1 : 20;

        LessonSlot slot = LessonSlot.builder()
                .course(course)
                .startTime(dto.getStartTime())
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
        LessonSlot slot = lessonSlotRepository.findById(slotId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();


        if (!slot.getEnrolledUsers().contains(user)) {
            return false;
        }

        slot.getEnrolledUsers().remove(user);
        lessonSlotRepository.save(slot);
        return true;
    }
}