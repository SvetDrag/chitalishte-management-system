package bg.whiteswallow.manager.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class PublicPagesApiTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void indexPage_isPubliclyAccessible() throws Exception {
        mockMvc().perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void eventsPage_isPubliclyAccessible() throws Exception {
        mockMvc().perform(get("/events"))
                .andExpect(status().isOk());
    }

    @Test
    void coursesPage_isPubliclyAccessible() throws Exception {
        mockMvc().perform(get("/courses"))
                .andExpect(status().isOk());
    }

    @Test
    void loginPage_isPubliclyAccessible() throws Exception {
        mockMvc().perform(get("/users/login"))
                .andExpect(status().isOk());
    }

    @Test
    void homePage_redirectsToLogin_whenUnauthenticated() throws Exception {
        mockMvc().perform(get("/home"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void eventsAddPage_redirectsToLogin_whenUnauthenticated() throws Exception {
        mockMvc().perform(get("/events/add"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void eventsAddPage_isForbidden_forRegularUser() throws Exception {
        mockMvc().perform(get("/events/add"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin1", roles = "ADMIN")
    void eventsAddPage_isAccessible_forAdmin() throws Exception {
        mockMvc().perform(get("/events/add"))
                .andExpect(status().isOk());
    }

    @Test
    void addEvent_withoutCsrfToken_isForbidden() throws Exception {
        mockMvc().perform(post("/events/add")
                        .param("title", "Test")
                        .param("description", "Test desc")
                        .param("eventDate", "2027-01-01T10:00")
                        .param("location", "Test location"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin1", roles = "ADMIN")
    void addEvent_withCsrfToken_redirectsToEvents() throws Exception {
        mockMvc().perform(post("/events/add")
                        .with(csrf())
                        .param("title", "Тестово събитие")
                        .param("description", "Описание на събитието")
                        .param("eventDate", "2027-01-01T10:00")
                        .param("location", "Голяма зала"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/events*"));
    }
}
