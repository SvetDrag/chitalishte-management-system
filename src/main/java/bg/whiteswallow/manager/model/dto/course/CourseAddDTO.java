package bg.whiteswallow.manager.model.dto.course;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CourseAddDTO {

    @NotBlank(message = "Името на дейността е задължително")
    private String name;

    @Positive(message = "Груповата такса трябва да е положителна")
    private BigDecimal groupPricePerLesson;

    @Positive(message = "Индивидуалната такса трябва да е положителна")
    private BigDecimal individualPricePerLesson;

    @NotNull(message = "Моля, изберете преподавател!")
    private UUID instructorId;
}
