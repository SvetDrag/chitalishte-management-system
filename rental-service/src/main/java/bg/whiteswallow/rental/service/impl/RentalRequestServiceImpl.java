package bg.whiteswallow.rental.service.impl;

import bg.whiteswallow.rental.dto.EquipmentItemResponseDTO;
import bg.whiteswallow.rental.dto.HallResponseDTO;
import bg.whiteswallow.rental.dto.RentalRequestCreateDTO;
import bg.whiteswallow.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.rental.dto.RentalStatusUpdateDTO;
import bg.whiteswallow.rental.entity.EquipmentItem;
import bg.whiteswallow.rental.entity.Hall;
import bg.whiteswallow.rental.entity.RentalRequest;
import bg.whiteswallow.rental.entity.RentalStatus;
import bg.whiteswallow.rental.exception.HallNotAvailableException;
import bg.whiteswallow.rental.exception.ResourceNotFoundException;
import bg.whiteswallow.rental.repository.EquipmentItemRepository;
import bg.whiteswallow.rental.repository.HallRepository;
import bg.whiteswallow.rental.repository.RentalRequestRepository;
import bg.whiteswallow.rental.service.RentalRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RentalRequestServiceImpl implements RentalRequestService {

    private static final Logger log = LoggerFactory.getLogger(RentalRequestServiceImpl.class);
    private static final List<RentalStatus> ACTIVE_STATUSES = List.of(RentalStatus.PENDING, RentalStatus.CONFIRMED);

    private final RentalRequestRepository rentalRequestRepository;
    private final HallRepository hallRepository;
    private final EquipmentItemRepository equipmentItemRepository;

    public RentalRequestServiceImpl(RentalRequestRepository rentalRequestRepository,
                                     HallRepository hallRepository,
                                     EquipmentItemRepository equipmentItemRepository) {
        this.rentalRequestRepository = rentalRequestRepository;
        this.hallRepository = hallRepository;
        this.equipmentItemRepository = equipmentItemRepository;
    }

    @Override
    @CacheEvict(value = "rentals", allEntries = true)
    public RentalRequestResponseDTO createRentalRequest(RentalRequestCreateDTO createDTO) {
        if (!createDTO.getEndDateTime().isAfter(createDTO.getStartDateTime())) {
            throw new IllegalArgumentException("Крайният час трябва да е след началния.");
        }

        Hall hall = hallRepository.findById(createDTO.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Залата не е намерена."));

        if (!checkAvailability(hall.getId(), createDTO.getStartDateTime(), createDTO.getEndDateTime())) {
            throw new HallNotAvailableException("Залата вече е заета за избрания период.");
        }

        List<EquipmentItem> equipmentItems = createDTO.getEquipmentItemIds() == null
                ? Collections.emptyList()
                : equipmentItemRepository.findAllById(createDTO.getEquipmentItemIds());

        RentalRequest rentalRequest = RentalRequest.builder()
                .hall(hall)
                .equipmentItems(equipmentItems)
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
        log.info("Created rental request {} for renter '{}' in hall '{}'",
                rentalRequest.getId(), rentalRequest.getRenterName(), hall.getName());
        return toResponseDTO(rentalRequest);
    }

    @Override
    @CacheEvict(value = "rentals", allEntries = true)
    public RentalRequestResponseDTO updateStatus(UUID id, RentalStatusUpdateDTO statusUpdateDTO) {
        RentalRequest rentalRequest = rentalRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заявката за наем не е намерена."));

        rentalRequest.setStatus(statusUpdateDTO.getStatus());
        rentalRequest = rentalRequestRepository.save(rentalRequest);
        log.info("Rental request {} status changed to {}", rentalRequest.getId(), rentalRequest.getStatus());
        return toResponseDTO(rentalRequest);
    }

    @Override
    @CacheEvict(value = "rentals", allEntries = true)
    public void deleteRentalRequest(UUID id) {
        if (!rentalRequestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Заявката за наем не е намерена.");
        }
        rentalRequestRepository.deleteById(id);
        log.info("Deleted rental request {}", id);
    }

    @Override
    @Cacheable("rentals")
    public List<RentalRequestResponseDTO> getAllRentalRequests() {
        return rentalRequestRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public boolean checkAvailability(UUID hallId, LocalDateTime from, LocalDateTime to) {
        List<RentalRequest> overlapping = rentalRequestRepository
                .findAllByHallIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(hallId, ACTIVE_STATUSES, to, from);
        return overlapping.isEmpty();
    }

    @Override
    @CacheEvict(value = "rentals", allEntries = true)
    public void completeExpiredRentals() {
        List<RentalRequest> expired = rentalRequestRepository
                .findAllByStatusAndEndDateTimeBefore(RentalStatus.CONFIRMED, LocalDateTime.now());

        for (RentalRequest rentalRequest : expired) {
            rentalRequest.setStatus(RentalStatus.COMPLETED);
        }

        rentalRequestRepository.saveAll(expired);
        if (!expired.isEmpty()) {
            log.info("Marked {} expired confirmed rental(s) as COMPLETED", expired.size());
        }
    }

    private RentalRequestResponseDTO toResponseDTO(RentalRequest rentalRequest) {
        return RentalRequestResponseDTO.builder()
                .id(rentalRequest.getId())
                .hall(toHallDTO(rentalRequest.getHall()))
                .equipmentItems(rentalRequest.getEquipmentItems().stream().map(this::toEquipmentDTO).toList())
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

    private HallResponseDTO toHallDTO(Hall hall) {
        return HallResponseDTO.builder()
                .id(hall.getId())
                .name(hall.getName())
                .capacity(hall.getCapacity())
                .description(hall.getDescription())
                .build();
    }

    private EquipmentItemResponseDTO toEquipmentDTO(EquipmentItem item) {
        return EquipmentItemResponseDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .pricePerRental(item.getPricePerRental())
                .available(item.isAvailable())
                .build();
    }
}
