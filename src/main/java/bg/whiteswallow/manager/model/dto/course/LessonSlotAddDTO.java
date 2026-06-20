package bg.whiteswallow.manager.model.dto.course;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class LessonSlotAddDTO {

    @NotNull(message = "Моля, изберете школа!")
    private UUID courseId;

    @NotNull(message = "Датата и часът са задължителни!")
    @FutureOrPresent(message = "Часът не може да е в миналото!")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;
}