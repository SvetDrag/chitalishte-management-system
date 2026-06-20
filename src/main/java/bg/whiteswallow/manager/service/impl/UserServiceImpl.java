package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.user.UserLoginDTO;
import bg.whiteswallow.manager.model.dto.user.UserRegisterDTO;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.model.entity.user.UserRole;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                .password(userRegisterDTO.getPassword())
                .role(userRepository.count() == 0 ? UserRole.ADMIN : UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return true;
    }

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        Optional<User> user = userRepository.findByUsername(userLoginDTO.getUsername());

        if (user.isEmpty() || !user.get().getPassword().equals(userLoginDTO.getPassword())) {
            return null;
        }

        return user.get();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
}