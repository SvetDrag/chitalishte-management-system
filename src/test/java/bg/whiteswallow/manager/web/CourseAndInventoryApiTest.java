package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.inventory.InventoryItem;
import bg.whiteswallow.manager.model.entity.inventory.ItemStatus;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.model.entity.user.UserRole;
import bg.whiteswallow.manager.repository.CourseRepository;
import bg.whiteswallow.manager.repository.InventoryItemRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class CourseAndInventoryApiTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    private User instructor;
    private Course course;
    private InventoryItem item;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    void setUp() {
        instructor = userRepository.save(User.builder()
                .username("instructor-" + System.nanoTime())
                .password("hashed")
                .email("instructor" + System.nanoTime() + "@example.com")
                .firstName("Инструктор")
                .lastName("Преподавателов")
                .role(UserRole.EMPLOYEE)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build());

        course = courseRepository.save(Course.builder()
                .name("Школа по тестове")
                .groupPricePerLesson(new BigDecimal("10"))
                .instructor(instructor)
                .build());

        item = inventoryItemRepository.save(InventoryItem.builder()
                .name("Тестов инвентар")
                .itemCondition("Ново")
                .status(ItemStatus.AVAILABLE)
                .build());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void courseEditPage_isAccessible_forAdmin() throws Exception {
        mockMvc().perform(get("/courses/edit/" + course.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmEditCourse_updatesCourseAndRedirects() throws Exception {
        mockMvc().perform(post("/courses/edit/" + course.getId())
                        .with(csrf())
                        .param("name", "Обновена школа")
                        .param("groupPricePerLesson", "15")
                        .param("instructorId", instructor.getId().toString()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCourse_redirectsToCourses() throws Exception {
        mockMvc().perform(post("/courses/delete/" + course.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void courseSchedulePage_isPubliclyAccessible() throws Exception {
        mockMvc().perform(get("/courses/" + course.getId() + "/schedule"))
                .andExpect(status().isOk());
    }

    @Test
    void inventoryPage_isPubliclyAccessible() throws Exception {
        mockMvc().perform(get("/inventory"))
                .andExpect(status().isOk());
    }

    @Test
    void inventoryAddPage_redirectsToLogin_whenUnauthenticated() throws Exception {
        mockMvc().perform(get("/inventory/add"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void inventoryAddPage_isAccessible_forAdmin() throws Exception {
        mockMvc().perform(get("/inventory/add"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmAddItem_createsItemAndRedirects() throws Exception {
        mockMvc().perform(post("/inventory/add")
                        .with(csrf())
                        .param("name", "Нов костюм")
                        .param("itemCondition", "Ново")
                        .param("status", "AVAILABLE"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void inventoryEditPage_isAccessible_forAdmin() throws Exception {
        mockMvc().perform(get("/inventory/edit/" + item.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void lendItemForm_isAccessible_forAdmin() throws Exception {
        mockMvc().perform(get("/inventory/lend/" + item.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmLendItem_lendsItemAndRedirects() throws Exception {
        User borrower = userRepository.save(User.builder()
                .username("borrower-" + System.nanoTime())
                .password("hashed")
                .email("borrower" + System.nanoTime() + "@example.com")
                .firstName("Наемател")
                .lastName("Тестов")
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build());

        mockMvc().perform(post("/inventory/lend/" + item.getId())
                        .with(csrf())
                        .param("userId", borrower.getId().toString()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void returnItem_redirectsToInventory() throws Exception {
        mockMvc().perform(post("/inventory/return/" + item.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteItem_redirectsToInventory() throws Exception {
        mockMvc().perform(post("/inventory/delete/" + item.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    void inventoryAddPage_isForbidden_forRegularUser() throws Exception {
        mockMvc().perform(get("/inventory/add"))
                .andExpect(status().isForbidden());
    }
}
