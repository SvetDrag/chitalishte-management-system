package bg.whiteswallow.manager.rental.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class HallResponseDTO {
    private UUID id;
    private String name;
    private int capacity;
    private String description;
}
