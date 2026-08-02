package bg.whiteswallow.rental.dto;

import bg.whiteswallow.rental.entity.RentalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentalStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private RentalStatus status;
}
