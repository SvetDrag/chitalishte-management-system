package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.model.entity.user.UserRole;
import bg.whiteswallow.manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserManagementApiTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    void setUp() {
        if (userRepository.findByUsername("profile-fixture").isEmpty()) {
            userRepository.save(User.builder()
                    .username("profile-fixture")
                    .password(passwordEncoder.encode("Passw0rd"))
                    .email("profile-fixture@example.com")
                    .firstName("Профил")
                    .lastName("Тестов")
                    .role(UserRole.USER)
                    .isActive(true)
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build());
        }
    }

    @Test
    void registerPage_isPubliclyAccessible() throws Exception {
        mockMvc().perform(get("/users/register"))
                .andExpect(status().isOk());
    }

    @Test
    void confirmRegister_createsUserAndRedirectsToLogin() throws Exception {
        mockMvc().perform(post("/users/register")
                        .with(csrf())
                        .param("username", "newmember" + System.nanoTime())
                        .param("firstName", "Нов")
                        .param("lastName", "Член")
                        .param("email", "newmember" + System.nanoTime() + "@example.com")
                        .param("password", "Passw0rd")
                        .param("confirmPassword", "Passw0rd"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void confirmRegister_redirectsBackToForm_whenValidationFails() throws Exception {
        mockMvc().perform(post("/users/register")
                        .with(csrf())
                        .param("username", "")
                        .param("password", "Passw0rd")
                        .param("confirmPassword", "Passw0rd"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails(value = "profile-fixture", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void profilePage_isAccessible_forAuthenticatedUser() throws Exception {
        mockMvc().perform(get("/users/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "profile-fixture", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void confirmProfile_updatesProfileAndRedirects() throws Exception {
        mockMvc().perform(post("/users/profile")
                        .with(csrf())
                        .param("firstName", "Обновено")
                        .param("lastName", "Име")
                        .param("email", "updated@example.com"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminUsersPage_isAccessible_forAdmin() throws Exception {
        mockMvc().perform(get("/users/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    void adminUsersPage_redirectsToLogin_whenUnauthenticated() throws Exception {
        mockMvc().perform(get("/users/admin/users"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails(value = "profile-fixture", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void changeRole_isForbidden_forNonAdmin() throws Exception {
        User target = userRepository.findByUsername("profile-fixture").orElseThrow();
        mockMvc().perform(post("/users/admin/role/" + target.getId())
                        .with(csrf())
                        .param("newRole", "ADMIN"))
                .andExpect(status().isForbidden());
    }
}
