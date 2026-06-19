package bg.whiteswallow.manager.model.dto.user;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDTO {

    @NotBlank(message = "Потребителското име е задължително!")
    @Size(min = 3, max = 20, message = "Потребителското име трябва да е между 3 и 20 символа!")
    private String username;

    @NotBlank(message = "Името е задължително!")
    @Size(min = 2, max = 30, message = "Въведете име")
    private String firstName;

    @NotBlank(message = "Фамилията е задължителна!")
    @Size(min = 2, max = 50, message = "Въведете фамилия")
    private String lastName;

    @NotBlank(message = "Имейлът е задължителен!")
    @Email(message = "Въведете валиден имейл адрес!")
    private String email;

    @NotBlank(message = "Паролата е задължителна!")
    @Size(min = 4, max = 20, message = "Паролата трябва да е между 4 и 20 символа!")
    private String password;

    @NotBlank(message = "Потвърждението на паролата е задължително!")
    private String confirmPassword;



}
