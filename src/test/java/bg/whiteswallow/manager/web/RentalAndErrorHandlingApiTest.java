package bg.whiteswallow.manager.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class RentalAndErrorHandlingApiTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rentalsPage_isAccessible_forAdmin() throws Exception {
        mockMvc().perform(get("/admin/rentals"))
                .andExpect(status().isOk());
    }

    @Test
    void rentalsPage_redirectsToLogin_whenUnauthenticated() throws Exception {
        mockMvc().perform(get("/admin/rentals"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    void rentalsPage_isForbidden_forRegularUser() throws Exception {
        mockMvc().perform(get("/admin/rentals"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmAddRental_redirectsBackToRentals_whenServiceUnavailable() throws Exception {
        mockMvc().perform(post("/admin/rentals/add")
                        .with(csrf())
                        .param("hallId", UUID.randomUUID().toString())
                        .param("renterName", "Тест Наемател")
                        .param("renterPhone", "0888000000")
                        .param("renterEmail", "test@example.com")
                        .param("startDateTime", "2027-01-01T10:00")
                        .param("endDateTime", "2027-01-01T14:00")
                        .param("price", "100")
                        .param("purpose", "Тест"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmAddHall_redirectsBackToRentals() throws Exception {
        mockMvc().perform(post("/admin/rentals/halls/add")
                        .with(csrf())
                        .param("name", "Тестова зала")
                        .param("capacity", "50"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteHall_redirectsBackToRentals() throws Exception {
        mockMvc().perform(post("/admin/rentals/halls/" + UUID.randomUUID() + "/delete").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmAddEquipment_redirectsBackToRentals() throws Exception {
        mockMvc().perform(post("/admin/rentals/equipment/add")
                        .with(csrf())
                        .param("name", "Тестова техника")
                        .param("pricePerRental", "20"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmRental_redirectsBackToRentals() throws Exception {
        mockMvc().perform(post("/admin/rentals/" + UUID.randomUUID() + "/confirm").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editingNonExistentCourse_returns404_notWhitelabel() throws Exception {
        mockMvc().perform(get("/courses/edit/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editingNonExistentEvent_returns404_notWhitelabel() throws Exception {
        mockMvc().perform(get("/events/edit/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void unmappedUrl_returns404_notWhitelabel() throws Exception {
        mockMvc().perform(get("/this-does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void accessDeniedPage_isPubliclyRenderable() throws Exception {
        mockMvc().perform(get("/access-denied"))
                .andExpect(status().isOk());
    }
}
