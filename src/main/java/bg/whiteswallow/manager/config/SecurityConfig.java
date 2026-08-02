package bg.whiteswallow.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/users/register", "/users/login", "/access-denied",
                                "/css/**", "/js/**", "/images/**", "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/events/add", "/events/edit/**", "/events/delete/**").hasRole("ADMIN")
                        .requestMatchers("/courses/add", "/courses/edit/**", "/courses/delete/**").hasRole("ADMIN")
                        .requestMatchers("/inventory/add", "/inventory/edit/**", "/inventory/delete/**",
                                "/inventory/lend/**", "/inventory/return/**").hasRole("ADMIN")
                        .requestMatchers("/users/admin/**", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/employee/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/events", "/courses", "/courses/*/schedule", "/inventory", "/schedule").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .loginProcessingUrl("/users/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/users/login?error")
                )
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/")
                )
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));

        return http.build();
    }
}
