package bg.whiteswallow.manager.model.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventAddDTO {

    @NotBlank(message = "Заглавието е задължително!")
    private String title;

    @NotBlank(message = "Описанието е задължително!")
    private String description;

    @NotNull(message = "Датата и часът са задължителни!")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime eventDate;

    @NotBlank(message = "Локацията е задължителна!")
    private String location;
}