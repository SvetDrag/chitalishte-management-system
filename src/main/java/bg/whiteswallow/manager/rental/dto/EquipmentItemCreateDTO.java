package bg.whiteswallow.manager.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EquipmentItemCreateDTO {

    @NotBlank(message = "Името на техниката е задължително!")
    private String name;

    @NotNull(message = "Цената е задължителна!")
    @Positive(message = "Цената трябва да е положителна!")
    private BigDecimal pricePerRental;
}
