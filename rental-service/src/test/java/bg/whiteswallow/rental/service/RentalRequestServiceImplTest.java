package bg.whiteswallow.rental.service;

import bg.whiteswallow.rental.dto.RentalRequestCreateDTO;
import bg.whiteswallow.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.rental.dto.RentalStatusUpdateDTO;
import bg.whiteswallow.rental.entity.Hall;
import bg.whiteswallow.rental.entity.RentalRequest;
import bg.whiteswallow.rental.entity.RentalStatus;
import bg.whiteswallow.rental.exception.HallNotAvailableException;
import bg.whiteswallow.rental.exception.ResourceNotFoundException;
import bg.whiteswallow.rental.repository.EquipmentItemRepository;
import bg.whiteswallow.rental.repository.HallRepository;
import bg.whiteswallow.rental.repository.RentalRequestRepository;
import bg.whiteswallow.rental.service.impl.RentalRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalRequestServiceImplTest {

    @Mock
    private RentalRequestRepository rentalRequestRepository;

    @Mock
    private HallRepository hallRepository;

    @Mock
    private EquipmentItemRepository equipmentItemRepository;

    @InjectMocks
    private RentalRequestServiceImpl rentalRequestService;

    private RentalRequestCreateDTO createDTO;
    private Hall hall;

    private static RentalStatusUpdateDTO statusUpdate(RentalStatus status) {
        RentalStatusUpdateDTO dto = new RentalStatusUpdateDTO();
        dto.setStatus(status);
        return dto;
    }

    @BeforeEach
    void setUp() {
        hall = Hall.builder().id(UUID.randomUUID()).name("Голяма зала").capacity(200).build();

        createDTO = new RentalRequestCreateDTO();
        createDTO.setHallId(hall.getId());
        createDTO.setRenterName("Ivan Ivanov");
        createDTO.setRenterPhone("0888123456");
        createDTO.setRenterEmail("ivan@example.com");
        createDTO.setStartDateTime(LocalDateTime.now().plusDays(1));
        createDTO.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(4));
        createDTO.setPurpose("Wedding");
        createDTO.setPrice(new BigDecimal("500"));

        lenient().when(hallRepository.findById(hall.getId())).thenReturn(Optional.of(hall));
    }

    @Test
    void createRentalRequest_savesAndReturnsDto_whenHallIsAvailable() {
        when(rentalRequestRepository.findAllByHallIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(rentalRequestRepository.save(any(RentalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RentalRequestResponseDTO result = rentalRequestService.createRentalRequest(createDTO);

        assertThat(result.getRenterName()).isEqualTo("Ivan Ivanov");
        assertThat(result.getStatus()).isEqualTo(RentalStatus.PENDING);
        assertThat(result.getHall().getId()).isEqualTo(hall.getId());
        verify(rentalRequestRepository).save(any(RentalRequest.class));
    }

    @Test
    void createRentalRequest_throwsResourceNotFound_whenHallMissing() {
        when(hallRepository.findById(hall.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalRequestService.createRentalRequest(createDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(rentalRequestRepository, never()).save(any());
    }

    @Test
    void createRentalRequest_throwsHallNotAvailable_whenOverlapExists() {
        RentalRequest existing = RentalRequest.builder().id(UUID.randomUUID()).status(RentalStatus.CONFIRMED).build();
        when(rentalRequestRepository.findAllByHallIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                any(), any(), any(), any())).thenReturn(List.of(existing));

        assertThatThrownBy(() -> rentalRequestService.createRentalRequest(createDTO))
                .isInstanceOf(HallNotAvailableException.class);

        verify(rentalRequestRepository, never()).save(any());
    }

    @Test
    void createRentalRequest_throwsIllegalArgument_whenEndBeforeStart() {
        createDTO.setEndDateTime(createDTO.getStartDateTime().minusHours(1));

        assertThatThrownBy(() -> rentalRequestService.createRentalRequest(createDTO))
                .isInstanceOf(IllegalArgumentException.class);

        verify(rentalRequestRepository, never()).save(any());
    }

    @Test
    void updateStatus_updatesAndReturnsDto_whenFound() {
        UUID id = UUID.randomUUID();
        RentalRequest existing = RentalRequest.builder().id(id).status(RentalStatus.PENDING).hall(hall).build();
        when(rentalRequestRepository.findById(id)).thenReturn(Optional.of(existing));
        when(rentalRequestRepository.save(any(RentalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RentalRequestResponseDTO result = rentalRequestService.updateStatus(id, statusUpdate(RentalStatus.CONFIRMED));

        assertThat(result.getStatus()).isEqualTo(RentalStatus.CONFIRMED);
    }

    @Test
    void updateStatus_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(rentalRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalRequestService.updateStatus(id, statusUpdate(RentalStatus.CONFIRMED)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRentalRequest_deletes_whenExists() {
        UUID id = UUID.randomUUID();
        when(rentalRequestRepository.existsById(id)).thenReturn(true);

        rentalRequestService.deleteRentalRequest(id);

        verify(rentalRequestRepository).deleteById(id);
    }

    @Test
    void deleteRentalRequest_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(rentalRequestRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> rentalRequestService.deleteRentalRequest(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(rentalRequestRepository, never()).deleteById(any());
    }

    @Test
    void checkAvailability_returnsFalse_whenOverlapExists() {
        when(rentalRequestRepository.findAllByHallIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                any(), any(), any(), any())).thenReturn(List.of(RentalRequest.builder().build()));

        boolean available = rentalRequestService.checkAvailability(hall.getId(), LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        assertThat(available).isFalse();
    }

    @Test
    void completeExpiredRentals_marksExpiredConfirmedRentalsAsCompleted() {
        RentalRequest expired = RentalRequest.builder().id(UUID.randomUUID()).status(RentalStatus.CONFIRMED).build();
        when(rentalRequestRepository.findAllByStatusAndEndDateTimeBefore(any(), any()))
                .thenReturn(List.of(expired));

        rentalRequestService.completeExpiredRentals();

        ArgumentCaptor<List<RentalRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(rentalRequestRepository, times(1)).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(RentalRequest::getStatus).containsExactly(RentalStatus.COMPLETED);
    }
}
