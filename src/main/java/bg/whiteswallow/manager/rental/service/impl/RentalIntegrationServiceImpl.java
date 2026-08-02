package bg.whiteswallow.manager.rental.service.impl;

import bg.whiteswallow.manager.rental.client.RentalServiceClient;
import bg.whiteswallow.manager.rental.dto.EquipmentItemCreateDTO;
import bg.whiteswallow.manager.rental.dto.EquipmentItemResponseDTO;
import bg.whiteswallow.manager.rental.dto.HallCreateDTO;
import bg.whiteswallow.manager.rental.dto.HallResponseDTO;
import bg.whiteswallow.manager.rental.dto.RentalRequestCreateDTO;
import bg.whiteswallow.manager.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.manager.rental.dto.RentalStatus;
import bg.whiteswallow.manager.rental.dto.RentalStatusUpdateDTO;
import bg.whiteswallow.manager.rental.service.RentalIntegrationService;
import bg.whiteswallow.manager.rental.service.RentalOperationResult;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RentalIntegrationServiceImpl implements RentalIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(RentalIntegrationServiceImpl.class);
    private static final String SERVICE_UNAVAILABLE_MSG = "Услугата за наеми е недостъпна в момента. Опитайте по-късно.";

    private final RentalServiceClient rentalServiceClient;

    public RentalIntegrationServiceImpl(RentalServiceClient rentalServiceClient) {
        this.rentalServiceClient = rentalServiceClient;
    }

    @Override
    public List<RentalRequestResponseDTO> getAllRentalRequests() {
        try {
            return rentalServiceClient.getAllRentalRequests();
        } catch (FeignException ex) {
            log.error("Failed to fetch rental requests from rental-service", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public RentalOperationResult createRentalRequest(RentalRequestCreateDTO createDTO) {
        try {
            RentalRequestResponseDTO created = rentalServiceClient.createRentalRequest(createDTO);
            log.info("Requested hall rental for '{}' via rental-service, id={}", created.getRenterName(), created.getId());
            return new RentalOperationResult(true, "Заявката за наем е изпратена успешно!");
        } catch (FeignException.Conflict ex) {
            log.warn("Rental conflict for renter '{}'", createDTO.getRenterName());
            return new RentalOperationResult(false, "Залата вече е заета за избрания период.");
        } catch (FeignException.NotFound ex) {
            log.warn("Hall {} not found when creating rental request", createDTO.getHallId());
            return new RentalOperationResult(false, "Избраната зала не е намерена.");
        } catch (FeignException ex) {
            log.error("Failed to create rental request via rental-service", ex);
            return new RentalOperationResult(false, SERVICE_UNAVAILABLE_MSG);
        }
    }

    @Override
    public RentalOperationResult updateStatus(UUID id, RentalStatus status) {
        try {
            rentalServiceClient.updateStatus(id, new RentalStatusUpdateDTO(status));
            log.info("Updated rental request {} status to {} via rental-service", id, status);
            return new RentalOperationResult(true, "Статусът е обновен успешно!");
        } catch (FeignException.NotFound ex) {
            return new RentalOperationResult(false, "Заявката за наем не е намерена.");
        } catch (FeignException ex) {
            log.error("Failed to update rental request status via rental-service", ex);
            return new RentalOperationResult(false, SERVICE_UNAVAILABLE_MSG);
        }
    }

    @Override
    public RentalOperationResult deleteRentalRequest(UUID id) {
        try {
            rentalServiceClient.deleteRentalRequest(id);
            log.info("Deleted rental request {} via rental-service", id);
            return new RentalOperationResult(true, "Заявката е изтрита успешно!");
        } catch (FeignException.NotFound ex) {
            return new RentalOperationResult(false, "Заявката за наем не е намерена.");
        } catch (FeignException ex) {
            log.error("Failed to delete rental request via rental-service", ex);
            return new RentalOperationResult(false, SERVICE_UNAVAILABLE_MSG);
        }
    }

    @Override
    public List<HallResponseDTO> getAllHalls() {
        try {
            return rentalServiceClient.getAllHalls();
        } catch (FeignException ex) {
            log.error("Failed to fetch halls from rental-service", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public RentalOperationResult createHall(HallCreateDTO createDTO) {
        try {
            HallResponseDTO created = rentalServiceClient.createHall(createDTO);
            log.info("Created hall '{}' via rental-service, id={}", created.getName(), created.getId());
            return new RentalOperationResult(true, "Залата е добавена успешно!");
        } catch (FeignException ex) {
            log.error("Failed to create hall via rental-service", ex);
            return new RentalOperationResult(false, SERVICE_UNAVAILABLE_MSG);
        }
    }

    @Override
    public RentalOperationResult deleteHall(UUID id) {
        try {
            rentalServiceClient.deleteHall(id);
            log.info("Deleted hall {} via rental-service", id);
            return new RentalOperationResult(true, "Залата е изтрита успешно!");
        } catch (FeignException.NotFound ex) {
            return new RentalOperationResult(false, "Залата не е намерена.");
        } catch (FeignException ex) {
            log.error("Failed to delete hall via rental-service", ex);
            return new RentalOperationResult(false, SERVICE_UNAVAILABLE_MSG);
        }
    }

    @Override
    public List<EquipmentItemResponseDTO> getAllEquipmentItems() {
        try {
            return rentalServiceClient.getAllEquipmentItems();
        } catch (FeignException ex) {
            log.error("Failed to fetch equipment items from rental-service", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public RentalOperationResult createEquipmentItem(EquipmentItemCreateDTO createDTO) {
        try {
            EquipmentItemResponseDTO created = rentalServiceClient.createEquipmentItem(createDTO);
            log.info("Created equipment item '{}' via rental-service, id={}", created.getName(), created.getId());
            return new RentalOperationResult(true, "Техниката е добавена успешно!");
        } catch (FeignException ex) {
            log.error("Failed to create equipment item via rental-service", ex);
            return new RentalOperationResult(false, SERVICE_UNAVAILABLE_MSG);
        }
    }

    @Override
    public RentalOperationResult deleteEquipmentItem(UUID id) {
        try {
            rentalServiceClient.deleteEquipmentItem(id);
            log.info("Deleted equipment item {} via rental-service", id);
            return new RentalOperationResult(true, "Техниката е изтрита успешно!");
        } catch (FeignException.NotFound ex) {
            return new RentalOperationResult(false, "Техниката не е намерена.");
        } catch (FeignException ex) {
            log.error("Failed to delete equipment item via rental-service", ex);
            return new RentalOperationResult(false, SERVICE_UNAVAILABLE_MSG);
        }
    }
}
