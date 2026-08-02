package bg.whiteswallow.manager.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HallCreateDTO {

    @NotBlank(message = "Името на залата е задължително!")
    private String name;

    @NotNull(message = "Капацитетът е задължителен!")
    @Positive(message = "Капацитетът трябва да е положителен!")
    private Integer capacity;

    private String description;
}
