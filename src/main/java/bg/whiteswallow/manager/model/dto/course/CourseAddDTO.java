package bg.whiteswallow.manager.model.dto.course;


import bg.whiteswallow.manager.model.entity.course.CourseType;
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

    @NotNull(message = "Моля, изберете тип на дейността")
    private CourseType type;

    @NotNull(message = "Въведете цена на урок!")
    @Positive(message = "Цената трябва да е положителна")
    private BigDecimal pricePerLesson;

    @NotNull(message = "Моля, изберете преподавател!")
    private UUID instructorId;
}
