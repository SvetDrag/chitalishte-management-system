package bg.whiteswallow.rental.web;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

class ApiKeyRequestInterceptor implements ClientHttpRequestInterceptor {

    static final String API_KEY_VALUE = "test-api-key-12345";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        request.getHeaders().add("X-API-KEY", API_KEY_VALUE);
        return execution.execute(request, body);
    }
}
