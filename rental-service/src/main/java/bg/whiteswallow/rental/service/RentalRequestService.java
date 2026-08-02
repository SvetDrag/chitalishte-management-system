package bg.whiteswallow.rental.service;

import bg.whiteswallow.rental.dto.RentalRequestCreateDTO;
import bg.whiteswallow.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.rental.dto.RentalStatusUpdateDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RentalRequestService {
    RentalRequestResponseDTO createRentalRequest(RentalRequestCreateDTO createDTO);
    RentalRequestResponseDTO updateStatus(UUID id, RentalStatusUpdateDTO statusUpdateDTO);
    void deleteRentalRequest(UUID id);
    List<RentalRequestResponseDTO> getAllRentalRequests();
    boolean checkAvailability(LocalDateTime from, LocalDateTime to);
}
