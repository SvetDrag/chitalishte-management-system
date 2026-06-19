package bg.whiteswallow.manager.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginDTO {

    @NotBlank(message = "Потребителското име е задължително!")
    private String username;

    @NotBlank(message = "Паролата е задължителна!")
    private String password;
}
