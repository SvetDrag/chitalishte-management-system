package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.service.LessonSlotService;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;


@Controller
public class HomeController {

    private final LessonSlotService lessonSlotService;

    public HomeController(LessonSlotService lessonSlotService) {
        this.lessonSlotService = lessonSlotService;
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



}
