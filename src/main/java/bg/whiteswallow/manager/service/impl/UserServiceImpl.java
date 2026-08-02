package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.user.UserProfileEditDTO;
import bg.whiteswallow.manager.model.dto.user.UserRegisterDTO;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.model.entity.user.UserRole;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean register(UserRegisterDTO userRegisterDTO) {
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getConfirmPassword())) {
            return false;
        }

        Optional<User> existingUser = userRepository.findByUsername(userRegisterDTO.getUsername());
        if (existingUser.isPresent()) {
            return false;
        }

        User user = User.builder()
                .username(userRegisterDTO.getUsername())
                .firstName(userRegisterDTO.getFirstName())
                .lastName(userRegisterDTO.getLastName())
                .email(userRegisterDTO.getEmail())
                .password(passwordEncoder.encode(userRegisterDTO.getPassword()))
                .role(userRepository.count() == 0 ? UserRole.ADMIN : UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        userRepository.save(user);
        log.info("Registered new user '{}' with role {}", user.getUsername(), user.getRole());
        return true;
    }

    @Override
    public void updateProfile(UUID id, UserProfileEditDTO userProfileEditDTO) {
        User user = userRepository.findById(id).orElseThrow();
        user.setFirstName(userProfileEditDTO.getFirstName());
        user.setLastName(userProfileEditDTO.getLastName());
        user.setEmail(userProfileEditDTO.getEmail());
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);
        log.info("User '{}' updated their profile", user.getUsername());
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    public void changeUserRole(UUID id, UserRole newRole) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Override
    public List<User> getAllEmployees() {
        return userRepository.findAllByRole(UserRole.EMPLOYEE);
    }
}