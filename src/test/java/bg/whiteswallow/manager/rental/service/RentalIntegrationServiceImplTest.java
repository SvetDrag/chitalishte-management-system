package bg.whiteswallow.manager.rental.service;

import bg.whiteswallow.manager.rental.client.RentalServiceClient;
import bg.whiteswallow.manager.rental.dto.HallCreateDTO;
import bg.whiteswallow.manager.rental.dto.HallResponseDTO;
import bg.whiteswallow.manager.rental.dto.RentalRequestCreateDTO;
import bg.whiteswallow.manager.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.manager.rental.dto.RentalStatus;
import bg.whiteswallow.manager.rental.service.impl.RentalIntegrationServiceImpl;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalIntegrationServiceImplTest {

    @Mock
    private RentalServiceClient rentalServiceClient;

    @InjectMocks
    private RentalIntegrationServiceImpl rentalIntegrationService;

    private static Request dummyRequest() {
        return Request.create(Request.HttpMethod.POST, "/api/rentals", Collections.emptyMap(), null, StandardCharsets.UTF_8);
    }

    private static FeignException conflict() {
        return new FeignException.Conflict("Conflict", dummyRequest(), null, Collections.emptyMap());
    }

    private static FeignException notFound() {
        return new FeignException.NotFound("Not found", dummyRequest(), null, Collections.emptyMap());
    }

    private static FeignException serverError() {
        return new FeignException.InternalServerError("Server error", dummyRequest(), null, Collections.emptyMap());
    }

    private RentalRequestCreateDTO createDTO() {
        RentalRequestCreateDTO dto = new RentalRequestCreateDTO();
        dto.setHallId(UUID.randomUUID());
        dto.setRenterName("Ivan");
        return dto;
    }

    @Test
    void createRentalRequest_returnsSuccess_whenClientSucceeds() {
        RentalRequestResponseDTO response = new RentalRequestResponseDTO();
        response.setId(UUID.randomUUID());
        response.setRenterName("Ivan");
        when(rentalServiceClient.createRentalRequest(any())).thenReturn(response);

        RentalOperationResult result = rentalIntegrationService.createRentalRequest(createDTO());

        assertThat(result.success()).isTrue();
    }

    @Test
    void createRentalRequest_returnsFailure_onConflict() {
        when(rentalServiceClient.createRentalRequest(any())).thenThrow(conflict());

        RentalOperationResult result = rentalIntegrationService.createRentalRequest(createDTO());

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("заета");
    }

    @Test
    void createRentalRequest_returnsFailure_onHallNotFound() {
        when(rentalServiceClient.createRentalRequest(any())).thenThrow(notFound());

        RentalOperationResult result = rentalIntegrationService.createRentalRequest(createDTO());

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("не е намерена");
    }

    @Test
    void createRentalRequest_returnsFailure_onServiceUnavailable() {
        when(rentalServiceClient.createRentalRequest(any())).thenThrow(serverError());

        RentalOperationResult result = rentalIntegrationService.createRentalRequest(createDTO());

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("недостъпна");
    }

    @Test
    void updateStatus_returnsSuccess_whenClientSucceeds() {
        UUID id = UUID.randomUUID();

        RentalOperationResult result = rentalIntegrationService.updateStatus(id, RentalStatus.CONFIRMED);

        assertThat(result.success()).isTrue();
    }

    @Test
    void updateStatus_returnsFailure_whenNotFound() {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(notFound()).when(rentalServiceClient).updateStatus(any(), any());

        RentalOperationResult result = rentalIntegrationService.updateStatus(id, RentalStatus.CONFIRMED);

        assertThat(result.success()).isFalse();
    }

    @Test
    void deleteRentalRequest_returnsSuccess_whenClientSucceeds() {
        UUID id = UUID.randomUUID();

        RentalOperationResult result = rentalIntegrationService.deleteRentalRequest(id);

        assertThat(result.success()).isTrue();
    }

    @Test
    void deleteRentalRequest_returnsFailure_whenNotFound() {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(notFound()).when(rentalServiceClient).deleteRentalRequest(id);

        RentalOperationResult result = rentalIntegrationService.deleteRentalRequest(id);

        assertThat(result.success()).isFalse();
    }

    @Test
    void getAllRentalRequests_returnsEmptyList_onClientFailure() {
        when(rentalServiceClient.getAllRentalRequests()).thenThrow(serverError());

        List<RentalRequestResponseDTO> result = rentalIntegrationService.getAllRentalRequests();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllRentalRequests_returnsClientResult() {
        List<RentalRequestResponseDTO> rentals = List.of(new RentalRequestResponseDTO());
        when(rentalServiceClient.getAllRentalRequests()).thenReturn(rentals);

        assertThat(rentalIntegrationService.getAllRentalRequests()).isEqualTo(rentals);
    }

    @Test
    void createHall_returnsSuccess_whenClientSucceeds() {
        HallResponseDTO hall = new HallResponseDTO();
        hall.setId(UUID.randomUUID());
        hall.setName("Голяма зала");
        when(rentalServiceClient.createHall(any())).thenReturn(hall);

        RentalOperationResult result = rentalIntegrationService.createHall(new HallCreateDTO());

        assertThat(result.success()).isTrue();
    }

    @Test
    void createHall_returnsFailure_onServiceUnavailable() {
        when(rentalServiceClient.createHall(any())).thenThrow(serverError());

        RentalOperationResult result = rentalIntegrationService.createHall(new HallCreateDTO());

        assertThat(result.success()).isFalse();
    }

    @Test
    void deleteHall_returnsFailure_whenNotFound() {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(notFound()).when(rentalServiceClient).deleteHall(id);

        RentalOperationResult result = rentalIntegrationService.deleteHall(id);

        assertThat(result.success()).isFalse();
    }

    @Test
    void getAllHalls_returnsEmptyList_onFailure() {
        when(rentalServiceClient.getAllHalls()).thenThrow(serverError());

        assertThat(rentalIntegrationService.getAllHalls()).isEmpty();
    }

    @Test
    void createEquipmentItem_returnsSuccess_whenClientSucceeds() {
        bg.whiteswallow.manager.rental.dto.EquipmentItemResponseDTO item = new bg.whiteswallow.manager.rental.dto.EquipmentItemResponseDTO();
        item.setId(UUID.randomUUID());
        item.setName("Прожектор");
        when(rentalServiceClient.createEquipmentItem(any())).thenReturn(item);

        RentalOperationResult result = rentalIntegrationService.createEquipmentItem(new bg.whiteswallow.manager.rental.dto.EquipmentItemCreateDTO());

        assertThat(result.success()).isTrue();
    }

    @Test
    void deleteEquipmentItem_returnsFailure_whenNotFound() {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(notFound()).when(rentalServiceClient).deleteEquipmentItem(id);

        RentalOperationResult result = rentalIntegrationService.deleteEquipmentItem(id);

        assertThat(result.success()).isFalse();
    }

    @Test
    void getAllEquipmentItems_returnsEmptyList_onFailure() {
        when(rentalServiceClient.getAllEquipmentItems()).thenThrow(serverError());

        assertThat(rentalIntegrationService.getAllEquipmentItems()).isEmpty();
    }
}
