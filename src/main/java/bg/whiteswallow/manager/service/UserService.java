package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.dto.user.UserLoginDTO;
import bg.whiteswallow.manager.model.dto.user.UserRegisterDTO;

public interface UserService {
    boolean register(UserRegisterDTO userRegisterDTO);
    boolean login(UserLoginDTO userLoginDTO);
}
