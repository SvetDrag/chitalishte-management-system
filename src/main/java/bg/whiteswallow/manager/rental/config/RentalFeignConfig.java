package bg.whiteswallow.manager.rental.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RentalFeignConfig {

    @Value("${rental-service.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor rentalServiceApiKeyInterceptor() {
        return requestTemplate -> requestTemplate.header("X-API-KEY", apiKey);
    }
}
