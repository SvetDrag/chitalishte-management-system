package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.security.UserPrincipal;
import bg.whiteswallow.manager.service.LessonSlotService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    private final LessonSlotService lessonSlotService;

    public ScheduleController(LessonSlotService lessonSlotService) {
        this.lessonSlotService = lessonSlotService;
    }

    @GetMapping
    public String publicSchedule(Model model) {
        model.addAttribute("upcomingSlots", lessonSlotService.getAllUpcomingSlots());
        return "schedule-public";
    }

    @PostMapping("/enroll/{slotId}")
    public String enroll(@PathVariable UUID slotId, @AuthenticationPrincipal UserPrincipal principal,
                         RedirectAttributes redirectAttributes, HttpServletRequest request) {
        boolean success = lessonSlotService.enrollUser(slotId, principal.getId());

        if (!success) {
            redirectAttributes.addFlashAttribute("errorMsg", "Неуспешно записване! Няма свободни места или вече сте записани.");
        } else {
            redirectAttributes.addFlashAttribute("successMsg", "Успешно запазихте своето място за урока!");
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/courses");
    }

    @PostMapping("/unenroll/{slotId}")
    public String unenroll(@PathVariable UUID slotId, @AuthenticationPrincipal UserPrincipal principal,
                           RedirectAttributes redirectAttributes, HttpServletRequest request) {
        boolean success = lessonSlotService.unenrollUser(slotId, principal.getId());

        if (success) {
            redirectAttributes.addFlashAttribute("successMsg", "Успешно освободихте мястото си за този час.");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Възникна грешка при отписването.");
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/courses");
    }
}