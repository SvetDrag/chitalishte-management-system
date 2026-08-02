package bg.whiteswallow.rental.dto;

import bg.whiteswallow.rental.entity.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
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
