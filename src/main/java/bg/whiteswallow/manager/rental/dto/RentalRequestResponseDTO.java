package bg.whiteswallow.manager.rental.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RentalRequestResponseDTO {
    private UUID id;
    private HallResponseDTO hall;
    private List<EquipmentItemResponseDTO> equipmentItems;
    private String renterName;
    private String renterPhone;
    private String renterEmail;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String purpose;
    private RentalStatus status;
    private BigDecimal price;
}
