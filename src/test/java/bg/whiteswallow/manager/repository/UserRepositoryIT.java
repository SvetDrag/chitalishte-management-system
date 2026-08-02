package bg.whiteswallow.manager.repository;

import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.model.entity.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    private User save(String username, String email, UserRole role) {
        User user = User.builder()
                .username(username)
                .password("hashed")
                .email(email)
                .firstName("Име")
                .lastName("Фамилия")
                .role(role)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    @Test
    void findByUsername_returnsUser_whenExists() {
        save("ivan123", "ivan@example.com", UserRole.USER);

        Optional<User> result = userRepository.findByUsername("ivan123");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void findByUsername_returnsEmpty_whenMissing() {
        Optional<User> result = userRepository.findByUsername("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_returnsUser_whenExists() {
        save("maria", "maria@example.com", UserRole.EMPLOYEE);

        Optional<User> result = userRepository.findByEmail("maria@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("maria");
    }

    @Test
    void findAllByRole_returnsOnlyMatchingUsers() {
        save("emp1", "emp1@example.com", UserRole.EMPLOYEE);
        save("emp2", "emp2@example.com", UserRole.EMPLOYEE);
        save("user1", "user1@example.com", UserRole.USER);

        List<User> employees = userRepository.findAllByRole(UserRole.EMPLOYEE);

        assertThat(employees).hasSize(2);
        assertThat(employees).extracting(User::getUsername).containsExactlyInAnyOrder("emp1", "emp2");
    }

    @Test
    void save_assignsGeneratedUuid() {
        User saved = save("newuser", "newuser@example.com", UserRole.USER);

        assertThat(saved.getId()).isNotNull();
    }
}
