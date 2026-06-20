package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.dto.course.CourseAddDTO;
import bg.whiteswallow.manager.model.entity.course.CourseType;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.service.CourseService;
import bg.whiteswallow.manager.service.LessonSlotService;
import bg.whiteswallow.manager.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;
    private final LessonSlotService lessonSlotService;

    public CourseController(CourseService courseService, UserService userService, LessonSlotService lessonSlotService) {
        this.courseService = courseService;
        this.userService = userService;
        this.lessonSlotService = lessonSlotService;
    }


    @ModelAttribute("employees")
    public List<User> employees() {
        return userService.getAllEmployees();
    }

    @ModelAttribute("courseAddDTO")
    public CourseAddDTO initCourseAddDTO(){
        return new CourseAddDTO();
    }

    @ModelAttribute("courseTypes")
    public CourseType[] courseTypes(){
        return CourseType.values();
    }

    @GetMapping
    public String courses(Model model) {
        model.addAttribute("allCourses", courseService.getAllCourses());
        return "courses";
    }

    @GetMapping("/add")
    public String addCourse(HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/courses";
        return "course-add";
    }

    @PostMapping("/add")
    public String confirmAddCourse(@Valid CourseAddDTO courseAddDTO,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/courses";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("courseAddDTO", courseAddDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.courseAddDTO", bindingResult);
            return "redirect:/courses/add";
        }

        courseService.addCourse(courseAddDTO);
        return "redirect:/courses";
    }

    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable UUID id, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/courses";
        courseService.deleteCourse(id);
        return "redirect:/courses";
    }

    @GetMapping("/edit/{id}")
    public String editCourse(@PathVariable UUID id, Model model, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/courses";
        if (!model.containsAttribute("courseEditDTO")) {
            model.addAttribute("courseEditDTO", courseService.getCourseForEdit(id));
        }
        model.addAttribute("courseId", id);
        return "course-edit";
    }

    @PostMapping("/edit/{id}")
    public String confirmEditCourse(@PathVariable UUID id,
                                    @Valid @ModelAttribute("courseEditDTO") CourseAddDTO courseEditDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/courses";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("courseEditDTO", courseEditDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.courseEditDTO", bindingResult);
            return "redirect:/courses/edit/" + id;
        }

        courseService.updateCourse(id, courseEditDTO);
        return "redirect:/courses";
    }

    @GetMapping("/{id}/schedule")
    public String courseSchedule(@PathVariable UUID id, Model model) {
        model.addAttribute("course", courseService.getCourseById(id));
        model.addAttribute("upcomingSlots", lessonSlotService.getUpcomingSlotsForCourse(id));
        return "course-schedule"; // Новият HTML файл
    }
}
