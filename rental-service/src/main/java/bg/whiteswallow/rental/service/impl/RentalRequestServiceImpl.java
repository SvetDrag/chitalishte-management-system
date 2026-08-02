package bg.whiteswallow.rental.service.impl;

import bg.whiteswallow.rental.dto.RentalRequestCreateDTO;
import bg.whiteswallow.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.rental.dto.RentalStatusUpdateDTO;
import bg.whiteswallow.rental.entity.RentalRequest;
import bg.whiteswallow.rental.entity.RentalStatus;
import bg.whiteswallow.rental.exception.HallNotAvailableException;
import bg.whiteswallow.rental.exception.ResourceNotFoundException;
import bg.whiteswallow.rental.repository.RentalRequestRepository;
import bg.whiteswallow.rental.service.RentalRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RentalRequestServiceImpl implements RentalRequestService {

    private static final Logger log = LoggerFactory.getLogger(RentalRequestServiceImpl.class);
    private static final List<RentalStatus> ACTIVE_STATUSES = List.of(RentalStatus.PENDING, RentalStatus.CONFIRMED);

    private final RentalRequestRepository rentalRequestRepository;

    public RentalRequestServiceImpl(RentalRequestRepository rentalRequestRepository) {
        this.rentalRequestRepository = rentalRequestRepository;
    }

    @Override
    public RentalRequestResponseDTO createRentalRequest(RentalRequestCreateDTO createDTO) {
        if (!createDTO.getEndDateTime().isAfter(createDTO.getStartDateTime())) {
            throw new IllegalArgumentException("Крайният час трябва да е след началния.");
        }

        if (!checkAvailability(createDTO.getStartDateTime(), createDTO.getEndDateTime())) {
            throw new HallNotAvailableException("Залата вече е заета за избрания период.");
        }

        RentalRequest rentalRequest = RentalRequest.builder()
                .renterName(createDTO.getRenterName())
                .renterPhone(createDTO.getRenterPhone())
                .renterEmail(createDTO.getRenterEmail())
                .startDateTime(createDTO.getStartDateTime())
                .endDateTime(createDTO.getEndDateTime())
                .purpose(createDTO.getPurpose())
                .price(createDTO.getPrice())
                .status(RentalStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .build();

        rentalRequest = rentalRequestRepository.save(rentalRequest);
        log.info("Created rental request {} for renter '{}'", rentalRequest.getId(), rentalRequest.getRenterName());
        return toResponseDTO(rentalRequest);
    }

    @Override
    public RentalRequestResponseDTO updateStatus(UUID id, RentalStatusUpdateDTO statusUpdateDTO) {
        RentalRequest rentalRequest = rentalRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заявката за наем не е намерена."));

        rentalRequest.setStatus(statusUpdateDTO.getStatus());
        rentalRequest = rentalRequestRepository.save(rentalRequest);
        log.info("Rental request {} status changed to {}", rentalRequest.getId(), rentalRequest.getStatus());
        return toResponseDTO(rentalRequest);
    }

    @Override
    public void deleteRentalRequest(UUID id) {
        if (!rentalRequestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Заявката за наем не е намерена.");
        }
        rentalRequestRepository.deleteById(id);
        log.info("Deleted rental request {}", id);
    }

    @Override
    public List<RentalRequestResponseDTO> getAllRentalRequests() {
        return rentalRequestRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public boolean checkAvailability(LocalDateTime from, LocalDateTime to) {
        List<RentalRequest> overlapping = rentalRequestRepository
                .findAllByStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(ACTIVE_STATUSES, to, from);
        return overlapping.isEmpty();
    }

    private RentalRequestResponseDTO toResponseDTO(RentalRequest rentalRequest) {
        return RentalRequestResponseDTO.builder()
                .id(rentalRequest.getId())
                .renterName(rentalRequest.getRenterName())
                .renterPhone(rentalRequest.getRenterPhone())
                .renterEmail(rentalRequest.getRenterEmail())
                .startDateTime(rentalRequest.getStartDateTime())
                .endDateTime(rentalRequest.getEndDateTime())
                .purpose(rentalRequest.getPurpose())
                .status(rentalRequest.getStatus())
                .price(rentalRequest.getPrice())
                .build();
    }
}
