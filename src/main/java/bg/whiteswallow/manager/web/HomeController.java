package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.entity.course.LessonAttendance;
import bg.whiteswallow.manager.service.LessonAttendanceService;
import bg.whiteswallow.manager.service.LessonSlotService;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Controller
public class HomeController {

    private final LessonSlotService lessonSlotService;
    private final LessonAttendanceService lessonAttendanceService;

    public HomeController(LessonSlotService lessonSlotService, LessonAttendanceService lessonAttendanceService) {
        this.lessonSlotService = lessonSlotService;
        this.lessonAttendanceService = lessonAttendanceService;
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        //If user is logged - redirect to the Dashboard
        if (session.getAttribute("user_id") != null){
            return "redirect:/home";
        }
        return "index"; //if is still guest -> home page
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        UUID userId = (UUID) session.getAttribute("user_id");
        if (userId == null) {
            return "redirect:/";
        }

        String role = (String) session.getAttribute("user_role");


        if ("USER".equals(role)) {
            model.addAttribute("myLessons", lessonSlotService.getUserUpcomingLessons(userId));
        }

        else if ("EMPLOYEE".equals(role)) {
            model.addAttribute("mySchedule", lessonSlotService.getInstructorSchedule(userId));
        }

        return "home";
    }

    @GetMapping("/admin/finances")
    public String showFinances(HttpSession session, Model model) {

        if (!"ADMIN".equals(session.getAttribute("user_role"))) {
            return "redirect:/home";
        }

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
