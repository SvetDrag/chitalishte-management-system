package bg.whiteswallow.manager.web.advice;

import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("currentUser")
    public User currentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return principal == null ? null : principal.getUser();
    }
}
