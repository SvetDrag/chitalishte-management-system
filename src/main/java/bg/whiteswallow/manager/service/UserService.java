package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.dto.user.UserLoginDTO;
import bg.whiteswallow.manager.model.dto.user.UserRegisterDTO;
import bg.whiteswallow.manager.model.entity.user.User;

public interface UserService {
    boolean register(UserRegisterDTO userRegisterDTO);
    User login(UserLoginDTO userLoginDTO); // Вече връща User вместо boolean
}
