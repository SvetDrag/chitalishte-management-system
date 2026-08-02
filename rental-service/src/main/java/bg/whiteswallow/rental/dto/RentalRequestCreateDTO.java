package bg.whiteswallow.rental.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class RentalRequestCreateDTO {

    @NotBlank(message = "Renter name is required")
    private String renterName;

    @NotBlank(message = "Renter phone is required")
    private String renterPhone;

    @NotBlank(message = "Renter email is required")
    @Email(message = "Provide a valid email address")
    private String renterEmail;

    @NotNull(message = "Start date and time are required")
    @FutureOrPresent(message = "Start date cannot be in the past")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date and time are required")
    private LocalDateTime endDateTime;

    @NotBlank(message = "Purpose is required")
    private String purpose;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
}
