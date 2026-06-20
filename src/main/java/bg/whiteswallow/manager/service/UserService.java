package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.dto.user.UserLoginDTO;
import bg.whiteswallow.manager.model.dto.user.UserRegisterDTO;
import bg.whiteswallow.manager.model.entity.user.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    boolean register(UserRegisterDTO userRegisterDTO);
    User login(UserLoginDTO userLoginDTO);
    List<User> getAllUsers();
    void deleteUser(UUID id);
}
