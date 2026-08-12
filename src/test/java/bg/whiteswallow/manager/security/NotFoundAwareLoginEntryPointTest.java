package bg.whiteswallow.manager.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotFoundAwareLoginEntryPointTest {

    @Mock
    private RequestMappingHandlerMapping handlerMapping;

    private NotFoundAwareLoginEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        entryPoint = new NotFoundAwareLoginEntryPoint(handlerMapping, "/users/login");
    }

    @Test
    void commence_returns404_whenNoControllerMatchesTheRequest() throws Exception {
        when(handlerMapping.getHandler(any())).thenReturn(null);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this-does-not-exist");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("not authenticated"));

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    void commence_returns404_whenHandlerLookupFails() throws Exception {
        when(handlerMapping.getHandler(any())).thenThrow(new IllegalStateException("lookup failed"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/whatever");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("not authenticated"));

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void commence_redirectsToLogin_whenRequestMatchesARealController() throws Exception {
        when(handlerMapping.getHandler(any())).thenReturn(mock(HandlerExecutionChain.class));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/schedule");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("not authenticated"));

        assertThat(response.getRedirectedUrl()).contains("/users/login");
    }
}
