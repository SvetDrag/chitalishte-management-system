package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.dto.user.UserProfileEditDTO;
import bg.whiteswallow.manager.model.dto.user.UserRegisterDTO;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.model.entity.user.UserRole;

import java.util.List;
import java.util.UUID;

public interface UserService {
    boolean register(UserRegisterDTO userRegisterDTO);
    List<User> getAllUsers();
    void deleteUser(UUID id);
    void changeUserRole(UUID id, UserRole newRole);
    List<User> getAllEmployees();
    void updateProfile(UUID id, UserProfileEditDTO userProfileEditDTO);
}
