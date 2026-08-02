package bg.whiteswallow.rental.web;

import bg.whiteswallow.rental.dto.AvailabilityResponseDTO;
import bg.whiteswallow.rental.dto.RentalRequestCreateDTO;
import bg.whiteswallow.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.rental.dto.RentalStatusUpdateDTO;
import bg.whiteswallow.rental.service.RentalRequestService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rentals")
public class RentalRequestController {

    private final RentalRequestService rentalRequestService;

    public RentalRequestController(RentalRequestService rentalRequestService) {
        this.rentalRequestService = rentalRequestService;
    }

    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponseDTO> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        boolean available = rentalRequestService.checkAvailability(from, to);
        return ResponseEntity.ok(new AvailabilityResponseDTO(available));
    }

    @GetMapping
    public ResponseEntity<List<RentalRequestResponseDTO>> getAllRentalRequests() {
        return ResponseEntity.ok(rentalRequestService.getAllRentalRequests());
    }

    @PostMapping
    public ResponseEntity<RentalRequestResponseDTO> createRentalRequest(@Valid @RequestBody RentalRequestCreateDTO createDTO) {
        RentalRequestResponseDTO created = rentalRequestService.createRentalRequest(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RentalRequestResponseDTO> updateStatus(@PathVariable UUID id,
                                                                   @Valid @RequestBody RentalStatusUpdateDTO statusUpdateDTO) {
        return ResponseEntity.ok(rentalRequestService.updateStatus(id, statusUpdateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRentalRequest(@PathVariable UUID id) {
        rentalRequestService.deleteRentalRequest(id);
        return ResponseEntity.noContent().build();
    }
}
