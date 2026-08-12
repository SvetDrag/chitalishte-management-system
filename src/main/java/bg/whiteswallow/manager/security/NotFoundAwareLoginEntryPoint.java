package bg.whiteswallow.manager.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

/**
 * Redirects anonymous users to the login page only for requests that actually
 * match a controller — otherwise the URL was never valid to begin with, so we
 * let it fall through as a 404 instead of implying "log in and it'll appear".
 */
public class NotFoundAwareLoginEntryPoint implements AuthenticationEntryPoint {

    private final RequestMappingHandlerMapping handlerMapping;
    private final LoginUrlAuthenticationEntryPoint loginEntryPoint;

    public NotFoundAwareLoginEntryPoint(RequestMappingHandlerMapping handlerMapping, String loginFormUrl) {
        this.handlerMapping = handlerMapping;
        this.loginEntryPoint = new LoginUrlAuthenticationEntryPoint(loginFormUrl);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException, ServletException {
        if (!matchesKnownController(request)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        loginEntryPoint.commence(request, response, authException);
    }

    private boolean matchesKnownController(HttpServletRequest request) {
        try {
            return handlerMapping.getHandler(request) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
