package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.course.CourseType;
import bg.whiteswallow.manager.model.entity.course.LessonSlot;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.model.entity.user.UserRole;
import bg.whiteswallow.manager.repository.CourseRepository;
import bg.whiteswallow.manager.repository.LessonSlotRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EmployeeAndScheduleApiTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonSlotRepository lessonSlotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User employee;
    private User student;
    private Course course;
    private LessonSlot slot;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    void setUp() {
        employee = userRepository.findByUsername("schedule-employee").orElseGet(() -> userRepository.save(User.builder()
                .username("schedule-employee")
                .password(passwordEncoder.encode("Passw0rd"))
                .email("schedule-employee@example.com")
                .firstName("Учител")
                .lastName("Тестов")
                .role(UserRole.EMPLOYEE)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build()));

        student = userRepository.findByUsername("schedule-student").orElseGet(() -> userRepository.save(User.builder()
                .username("schedule-student")
                .password(passwordEncoder.encode("Passw0rd"))
                .email("schedule-student@example.com")
                .firstName("Ученик")
                .lastName("Тестов")
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build()));

        course = courseRepository.save(Course.builder()
                .name("Школа за график")
                .groupPricePerLesson(new BigDecimal("10"))
                .instructor(employee)
                .build());

        slot = lessonSlotRepository.save(LessonSlot.builder()
                .course(course)
                .startTime(LocalDateTime.now().plusDays(3))
                .type(CourseType.GROUP)
                .maxCapacity(20)
                .enrolledUsers(new ArrayList<>())
                .build());
    }

    @Test
    @WithUserDetails(value = "schedule-employee", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void employeeSchedulePage_isAccessible_forEmployee() throws Exception {
        mockMvc().perform(get("/employee/schedule"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "schedule-employee", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void confirmAddSlot_createsSlotAndRedirects() throws Exception {
        mockMvc().perform(post("/employee/schedule/add")
                        .with(csrf())
                        .param("courseId", course.getId().toString())
                        .param("type", "GROUP")
                        .param("startTime", "2027-01-15T18:00"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails(value = "schedule-employee", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void reportPage_isAccessible_forEmployee() throws Exception {
        mockMvc().perform(get("/employee/schedule/report/" + slot.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void employeeSchedulePage_redirectsToLogin_whenUnauthenticated() throws Exception {
        mockMvc().perform(get("/employee/schedule"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails(value = "schedule-student", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void homePage_showsUserDashboard_forRegularUser() throws Exception {
        mockMvc().perform(get("/home"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "schedule-employee", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void homePage_showsEmployeeDashboard_forEmployee() throws Exception {
        mockMvc().perform(get("/home"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "schedule-student", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void enroll_addsUserToSlotAndRedirects() throws Exception {
        mockMvc().perform(post("/schedule/enroll/" + slot.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails(value = "schedule-student", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void unenroll_removesUserFromSlotAndRedirects() throws Exception {
        mockMvc().perform(post("/schedule/enroll/" + slot.getId()).with(csrf()));

        mockMvc().perform(post("/schedule/unenroll/" + slot.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void schedulePublicPage_isPubliclyAccessible() throws Exception {
        mockMvc().perform(get("/schedule"))
                .andExpect(status().isOk());
    }
}
