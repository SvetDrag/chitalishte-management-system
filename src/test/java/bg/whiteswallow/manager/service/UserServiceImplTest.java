package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.exception.DuplicateUsernameException;
import bg.whiteswallow.manager.exception.PasswordMismatchException;
import bg.whiteswallow.manager.exception.ResourceNotFoundException;
import bg.whiteswallow.manager.model.dto.user.UserProfileEditDTO;
import bg.whiteswallow.manager.model.dto.user.UserRegisterDTO;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.model.entity.user.UserRole;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegisterDTO registerDTO;

    @BeforeEach
    void setUp() {
        registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("ivan123");
        registerDTO.setFirstName("Иван");
        registerDTO.setLastName("Иванов");
        registerDTO.setEmail("ivan@example.com");
        registerDTO.setPassword("pass1234");
        registerDTO.setConfirmPassword("pass1234");
    }

    @Test
    void register_throwsPasswordMismatch_whenPasswordsDoNotMatch() {
        registerDTO.setConfirmPassword("different");

        assertThatThrownBy(() -> userService.register(registerDTO))
                .isInstanceOf(PasswordMismatchException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsDuplicateUsername_whenUsernameTaken() {
        when(userRepository.findByUsername("ivan123")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.register(registerDTO))
                .isInstanceOf(DuplicateUsernameException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_savesHashedPasswordAndGrantsAdminToFirstUser() {
        when(userRepository.findByUsername("ivan123")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("pass1234")).thenReturn("hashed-password");

        userService.register(registerDTO);

        verify(userRepository).save(argThatUserRole(UserRole.ADMIN));
    }

    @Test
    void register_grantsUserRole_whenNotFirstUser() {
        when(userRepository.findByUsername("ivan123")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode("pass1234")).thenReturn("hashed-password");

        userService.register(registerDTO);

        verify(userRepository).save(argThatUserRole(UserRole.USER));
    }

    private User argThatUserRole(UserRole role) {
        return org.mockito.ArgumentMatchers.argThat(u -> u != null && u.getRole() == role);
    }

    @Test
    void updateProfile_updatesFields_whenUserFound() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).username("ivan123").firstName("Old").lastName("Name").email("old@example.com").build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserProfileEditDTO editDTO = new UserProfileEditDTO();
        editDTO.setFirstName("Нов");
        editDTO.setLastName("Профил");
        editDTO.setEmail("new@example.com");

        userService.updateProfile(id, editDTO);

        assertThat(user.getFirstName()).isEqualTo("Нов");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(id, new UserProfileEditDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changeUserRole_updatesRole_whenFound() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).username("ivan123").role(UserRole.USER).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.changeUserRole(id, UserRole.EMPLOYEE);

        assertThat(user.getRole()).isEqualTo(UserRole.EMPLOYEE);
        verify(userRepository).save(user);
    }

    @Test
    void changeUserRole_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeUserRole(id, UserRole.ADMIN))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUser_delegatesToRepository() {
        UUID id = UUID.randomUUID();

        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
    }

    @Test
    void getAllUsers_returnsRepositoryResult() {
        List<User> users = List.of(new User());
        when(userRepository.findAll()).thenReturn(users);

        assertThat(userService.getAllUsers()).isEqualTo(users);
    }

    @Test
    void getAllEmployees_returnsEmployeesOnly() {
        List<User> employees = List.of(User.builder().role(UserRole.EMPLOYEE).build());
        when(userRepository.findAllByRole(UserRole.EMPLOYEE)).thenReturn(employees);

        assertThat(userService.getAllEmployees()).isEqualTo(employees);
    }
}
