package bg.whiteswallow.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ChitalishteManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChitalishteManagementSystemApplication.class, args);
    }

}
