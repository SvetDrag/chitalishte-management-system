package bg.whiteswallow.manager.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileEditDTO {

    @NotBlank(message = "Името е задължително!")
    @Size(min = 2, max = 30, message = "Въведете име")
    private String firstName;

    @NotBlank(message = "Фамилията е задължителна!")
    @Size(min = 2, max = 50, message = "Въведете фамилия")
    private String lastName;

    @NotBlank(message = "Имейлът е задължителен!")
    @Email(message = "Въведете валиден имейл адрес!")
    private String email;
}
