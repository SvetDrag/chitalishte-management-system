package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.dto.course.LessonSlotAddDTO;
import bg.whiteswallow.manager.model.entity.course.CourseType;
import bg.whiteswallow.manager.security.UserPrincipal;
import bg.whiteswallow.manager.service.CourseService;
import bg.whiteswallow.manager.service.LessonAttendanceService;
import bg.whiteswallow.manager.service.LessonSlotService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    public EmployeeController(LessonSlotService lessonSlotService, CourseService courseService, LessonAttendanceService attendanceService) {
        this.lessonSlotService = lessonSlotService;
        this.courseService = courseService;
        this.attendanceService = attendanceService;
    }

    @ModelAttribute("lessonSlotAddDTO")
    public LessonSlotAddDTO initDTO() {
        return new LessonSlotAddDTO();
    }

    @ModelAttribute("courseTypes")
    public CourseType[] courseTypes() {
        return CourseType.values();
    }

    @GetMapping("/schedule")
    public String schedule(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("myCourses", courseService.getCoursesByInstructor(principal.getId()));
        model.addAttribute("mySchedule", lessonSlotService.getInstructorSchedule(principal.getId()));

        return "employee-schedule";
    }

    @PostMapping("/schedule/add")
    public String confirmAddSlot(@Valid LessonSlotAddDTO lessonSlotAddDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal UserPrincipal principal) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("lessonSlotAddDTO", lessonSlotAddDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.lessonSlotAddDTO", bindingResult);
            return "redirect:/employee/schedule";
        }

        lessonSlotService.addSlot(lessonSlotAddDTO, principal.getId());

        return "redirect:/employee/schedule";
    }

    @GetMapping("/schedule/report/{slotId}")
    public String reportPage(@PathVariable UUID slotId, Model model) {
        model.addAttribute("slot", lessonSlotService.getSlotById(slotId));
        model.addAttribute("pendingUsers", attendanceService.getPendingUsersForSlot(slotId));
        model.addAttribute("reportedAttendances", attendanceService.getAttendancesForSlot(slotId));

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