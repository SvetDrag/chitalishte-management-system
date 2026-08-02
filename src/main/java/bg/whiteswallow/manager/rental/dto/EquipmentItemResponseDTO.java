package bg.whiteswallow.manager.rental.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class EquipmentItemResponseDTO {
    private UUID id;
    private String name;
    private BigDecimal pricePerRental;
    private boolean available;
}
