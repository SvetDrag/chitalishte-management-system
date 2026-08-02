package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.entity.course.LessonAttendance;
import bg.whiteswallow.manager.model.entity.user.UserRole;
import bg.whiteswallow.manager.security.UserPrincipal;
import bg.whiteswallow.manager.service.LessonAttendanceService;
import bg.whiteswallow.manager.service.LessonSlotService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class HomeController {

    private final LessonSlotService lessonSlotService;
    private final LessonAttendanceService lessonAttendanceService;

    public HomeController(LessonSlotService lessonSlotService, LessonAttendanceService lessonAttendanceService) {
        this.lessonSlotService = lessonSlotService;
        this.lessonAttendanceService = lessonAttendanceService;
    }

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            return "redirect:/home";
        }
        return "index";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        UserRole role = principal.getUser().getRole();

        if (role == UserRole.USER) {
            model.addAttribute("myLessons", lessonSlotService.getUserUpcomingLessons(principal.getId()));
        } else if (role == UserRole.EMPLOYEE) {
            model.addAttribute("mySchedule", lessonSlotService.getInstructorSchedule(principal.getId()));
        }

        return "home";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/admin/finances")
    public String showFinances(Model model) {
        List<LessonAttendance> allAttendances = lessonAttendanceService.getAllAttendances();

        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalUnpaid = BigDecimal.ZERO;

        for (LessonAttendance attendance : allAttendances) {
            BigDecimal price = attendance.getCourse().getPricePerLesson();

            if (attendance.isPaid()) {
                totalPaid = totalPaid.add(price);
            } else {

                totalUnpaid = totalUnpaid.add(price);
            }
        }

        model.addAttribute("attendances", allAttendances);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalUnpaid", totalUnpaid);

        return "admin-finances";
    }



}
