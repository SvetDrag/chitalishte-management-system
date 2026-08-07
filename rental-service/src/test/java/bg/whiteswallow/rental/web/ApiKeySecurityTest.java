package bg.whiteswallow.rental.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class ApiKeySecurityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void getHalls_returns401_whenApiKeyMissing() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/halls"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getHalls_returns401_whenApiKeyWrong() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "wrong-key");

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/halls"), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getHalls_returns200_whenApiKeyValid() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", ApiKeyRequestInterceptor.API_KEY_VALUE);

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/halls"), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
