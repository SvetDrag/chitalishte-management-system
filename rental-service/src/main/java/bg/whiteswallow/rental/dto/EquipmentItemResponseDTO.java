package bg.whiteswallow.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class EquipmentItemResponseDTO {
    private UUID id;
    private String name;
    private BigDecimal pricePerRental;
    private boolean available;
}
