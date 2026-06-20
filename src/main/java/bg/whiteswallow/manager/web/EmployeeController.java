package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.dto.course.LessonSlotAddDTO;
import bg.whiteswallow.manager.service.CourseService;
import bg.whiteswallow.manager.service.LessonSlotService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    private final LessonSlotService lessonSlotService;
    private final CourseService courseService;

    public EmployeeController(LessonSlotService lessonSlotService, CourseService courseService) {
        this.lessonSlotService = lessonSlotService;
        this.courseService = courseService;
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
}