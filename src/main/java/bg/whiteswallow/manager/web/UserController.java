package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.dto.user.UserLoginDTO;
import bg.whiteswallow.manager.model.dto.user.UserRegisterDTO;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.service.UserService;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("userRegisterDTO")
    public UserRegisterDTO initRegisterDTO() {
        return new UserRegisterDTO();
    }

    @ModelAttribute("userLoginDTO")
    public UserLoginDTO initLoginDTO() {
        return new UserLoginDTO();
    }

    @GetMapping("/register")
    public String register(HttpSession session) {
        if (session.getAttribute("user_id") != null) {
            return "redirect:/home";
        }
        return "register";
    }

    @PostMapping("/register")
    public String confirmRegister(@Valid UserRegisterDTO userRegisterDTO,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors() || !userService.register(userRegisterDTO)) {
            redirectAttributes.addFlashAttribute("userRegisterDTO", userRegisterDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegisterDTO", bindingResult);
            return "redirect:/users/register";
        }

        return "redirect:/users/login";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("user_id") != null) {
            return "redirect:/home";
        }
        return "login";
    }

    @PostMapping("/login")
    public String confirmLogin(@Valid UserLoginDTO userLoginDTO,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userLoginDTO", userLoginDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userLoginDTO", bindingResult);
            return "redirect:/users/login";
        }

        User loggedUser = userService.login(userLoginDTO);

        if (loggedUser == null) {
            redirectAttributes.addFlashAttribute("userLoginDTO", userLoginDTO);
            redirectAttributes.addFlashAttribute("badCredentials", true);
            return "redirect:/users/login";
        }

        session.setAttribute("user_id", loggedUser.getId());
        session.setAttribute("first_name", loggedUser.getFirstName());
        session.setAttribute("user_role", loggedUser.getRole().name());

        return "redirect:/home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/admin/users")
    public String listUsers(Model model, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/home";
        model.addAttribute("allUsers", userService.getAllUsers());
        return "admin-users";
    }

    @PostMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable UUID id, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/home";
        userService.deleteUser(id);
        return "redirect:/users/admin/users";
    }
}