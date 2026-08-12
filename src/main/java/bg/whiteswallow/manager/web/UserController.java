package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.exception.DuplicateUsernameException;
import bg.whiteswallow.manager.exception.PasswordMismatchException;
import bg.whiteswallow.manager.model.dto.user.UserLoginDTO;
import bg.whiteswallow.manager.model.dto.user.UserProfileEditDTO;
import bg.whiteswallow.manager.model.dto.user.UserRegisterDTO;
import bg.whiteswallow.manager.model.entity.user.UserRole;
import bg.whiteswallow.manager.security.UserPrincipal;
import bg.whiteswallow.manager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String register(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            return "redirect:/home";
        }
        return "register";
    }

    @PostMapping("/register")
    public String confirmRegister(@Valid UserRegisterDTO userRegisterDTO,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {

        if (!bindingResult.hasErrors()) {
            try {
                userService.register(userRegisterDTO);
                return "redirect:/users/login";
            } catch (DuplicateUsernameException e) {
                bindingResult.rejectValue("username", "username.duplicate", e.getMessage());
            } catch (PasswordMismatchException e) {
                bindingResult.rejectValue("confirmPassword", "password.mismatch", e.getMessage());
            }
        }

        redirectAttributes.addFlashAttribute("userRegisterDTO", userRegisterDTO);
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegisterDTO", bindingResult);
        return "redirect:/users/register";
    }

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (!model.containsAttribute("userProfileEditDTO")) {
            UserProfileEditDTO dto = new UserProfileEditDTO();
            dto.setFirstName(principal.getUser().getFirstName());
            dto.setLastName(principal.getUser().getLastName());
            dto.setEmail(principal.getUser().getEmail());
            model.addAttribute("userProfileEditDTO", dto);
        }
        return "profile";
    }

    @PostMapping("/profile")
    public String confirmProfile(@AuthenticationPrincipal UserPrincipal principal,
                                 @Valid UserProfileEditDTO userProfileEditDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userProfileEditDTO", userProfileEditDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userProfileEditDTO", bindingResult);
            return "redirect:/users/profile";
        }

        userService.updateProfile(principal.getId(), userProfileEditDTO);
        redirectAttributes.addFlashAttribute("profileUpdated", true);
        return "redirect:/users/profile";
    }

    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        model.addAttribute("allUsers", userService.getAllUsers());
        return "admin-users";
    }

    @PostMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return "redirect:/users/admin/users";
    }

    @PostMapping("/admin/role/{id}")
    public String changeRole(@PathVariable UUID id,
                             @RequestParam("newRole") UserRole newRole,
                             @AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getId().equals(id)) {
            return "redirect:/users/admin/users";
        }

        userService.changeUserRole(id, newRole);
        return "redirect:/users/admin/users";
    }
}