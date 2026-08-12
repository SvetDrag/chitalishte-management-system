package bg.whiteswallow.manager.web.advice;

import bg.whiteswallow.manager.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(NoResourceFoundException ex, Model model) {
        log.warn("No resource found for path: {}", ex.getResourcePath());
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("message", "Страницата не е намерена.");
        return "error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex, Model model) {
        log.warn("Invalid path/request parameter '{}': {}", ex.getName(), ex.getMessage());
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("message", "Страницата не е намерена.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpected(Exception ex, Model model) {
        log.error("Unhandled exception", ex);
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("message", "Възникна неочаквана грешка. Опитайте отново по-късно.");
        return "error";
    }
}
