package bg.whiteswallow.manager.rental.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class RentalRequestCreateDTO {

    @NotBlank(message = "Името на наемателя е задължително!")
    private String renterName;

    @NotBlank(message = "Телефонът е задължителен!")
    private String renterPhone;

    @NotBlank(message = "Имейлът е задължителен!")
    @Email(message = "Въведете валиден имейл адрес!")
    private String renterEmail;

    @NotNull(message = "Началната дата и час са задължителни!")
    @FutureOrPresent(message = "Началото не може да е в миналото!")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDateTime;

    @NotNull(message = "Крайната дата и час са задължителни!")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDateTime;

    @NotBlank(message = "Целта на наема е задължителна!")
    private String purpose;

    @NotNull(message = "Цената е задължителна!")
    @Positive(message = "Цената трябва да е положителна!")
    private BigDecimal price;
}
