package bg.whiteswallow.manager.rental.service;

import bg.whiteswallow.manager.rental.dto.RentalRequestCreateDTO;
import bg.whiteswallow.manager.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.manager.rental.dto.RentalStatus;

import java.util.List;
import java.util.UUID;

public interface RentalIntegrationService {
    List<RentalRequestResponseDTO> getAllRentalRequests();
    RentalOperationResult createRentalRequest(RentalRequestCreateDTO createDTO);
    RentalOperationResult updateStatus(UUID id, RentalStatus status);
    RentalOperationResult deleteRentalRequest(UUID id);
}
