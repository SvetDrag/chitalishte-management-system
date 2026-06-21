package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.dto.course.LessonSlotAddDTO;
import bg.whiteswallow.manager.model.entity.course.LessonAttendance;
import bg.whiteswallow.manager.model.entity.course.LessonSlot;
import bg.whiteswallow.manager.repository.LessonAttendanceRepository;
import bg.whiteswallow.manager.repository.LessonSlotRepository;
import bg.whiteswallow.manager.service.CourseService;
import bg.whiteswallow.manager.service.LessonAttendanceService;
import bg.whiteswallow.manager.service.LessonSlotService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    private final LessonSlotService lessonSlotService;
    private final CourseService courseService;
    private final LessonAttendanceService attendanceService;
    private final LessonSlotRepository lessonSlotRepository;
    private final LessonAttendanceRepository attendanceRepository;

    public EmployeeController(LessonSlotService lessonSlotService, LessonSlotRepository lessonSlotRepository, CourseService courseService, LessonAttendanceService attendanceService, LessonAttendanceRepository attendanceRepository) {
        this.lessonSlotService = lessonSlotService;
        this.courseService = courseService;
        this.attendanceService = attendanceService;
        this.lessonSlotRepository = lessonSlotRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @ModelAttribute("lessonSlotAddDTO")
    public LessonSlotAddDTO initDTO() {
        return new LessonSlotAddDTO();
    }

    @GetMapping("/schedule")
    public String schedule(Model model, HttpSession session) {
        String role = (String) session.getAttribute("user_role");
        if (role == null || (!role.equals("EMPLOYEE") && !role.equals("ADMIN"))) {
            return "redirect:/home";
        }

        UUID instructorId = (UUID) session.getAttribute("user_id");

       model.addAttribute("myCourses", courseService.getCoursesByInstructor(instructorId));
        model.addAttribute("mySchedule", lessonSlotService.getInstructorSchedule(instructorId));

        return "employee-schedule";
    }

    @PostMapping("/schedule/add")
    public String confirmAddSlot(@Valid LessonSlotAddDTO lessonSlotAddDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {

        String role = (String) session.getAttribute("user_role");
        if (role == null || (!role.equals("EMPLOYEE") && !role.equals("ADMIN"))) {
            return "redirect:/home";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("lessonSlotAddDTO", lessonSlotAddDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.lessonSlotAddDTO", bindingResult);
            return "redirect:/employee/schedule";
        }

        UUID instructorId = (UUID) session.getAttribute("user_id");
        lessonSlotService.addSlot(lessonSlotAddDTO, instructorId);

        return "redirect:/employee/schedule";
    }

    @GetMapping("/schedule/report/{slotId}")
    public String reportPage(@PathVariable UUID slotId, Model model, HttpSession session) {
        if (!"EMPLOYEE".equals(session.getAttribute("user_role")) && !"ADMIN".equals(session.getAttribute("user_role"))) {
            return "redirect:/home";
        }

        bg.whiteswallow.manager.model.entity.course.LessonSlot slot = lessonSlotRepository.findById(slotId).orElseThrow();


        java.util.List<LessonAttendance> reportedAttendances = attendanceRepository.findAllByLessonSlotId(slot.getId());


        java.util.List<UUID> reportedUserIds = reportedAttendances.stream()
                .map(a -> a.getUser().getId())
                .toList();


        java.util.List<bg.whiteswallow.manager.model.entity.user.User> pendingUsers = slot.getEnrolledUsers().stream()
                .filter(u -> !reportedUserIds.contains(u.getId()))
                .toList();

        model.addAttribute("slot", slot);
        model.addAttribute("pendingUsers", pendingUsers);
        model.addAttribute("reportedAttendances", reportedAttendances); // Подаваме самите записи!

        return "employee-report";
    }

        @PostMapping("/schedule/report/{slotId}/attendance/{attendanceId}/delete")
    public String deleteAttendance(@PathVariable UUID slotId, @PathVariable UUID attendanceId) {
        attendanceService.removeAttendance(attendanceId);
        return "redirect:/employee/schedule/report/" + slotId;
    }

        @PostMapping("/schedule/report/{slotId}/attendance/{attendanceId}/toggle")
    public String togglePayment(@PathVariable UUID slotId, @PathVariable UUID attendanceId) {
        attendanceService.togglePayment(attendanceId);
        return "redirect:/employee/schedule/report/" + slotId;
    }

    @PostMapping("/schedule/report/{slotId}/user/{userId}")
    public String markUser(@PathVariable UUID slotId, @PathVariable UUID userId, @RequestParam(name = "isPaid", defaultValue = "false") boolean isPaid) {
        attendanceService.markAttendance(userId, slotId, isPaid);
        return "redirect:/employee/schedule/report/" + slotId;
    }
}