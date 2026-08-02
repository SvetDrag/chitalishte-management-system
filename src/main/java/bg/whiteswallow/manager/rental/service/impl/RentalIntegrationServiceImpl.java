package bg.whiteswallow.manager.rental.service.impl;

import bg.whiteswallow.manager.rental.client.RentalServiceClient;
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
        } catch (FeignException ex) {
            log.error("Failed to create rental request via rental-service", ex);
            return new RentalOperationResult(false, "Услугата за наеми е недостъпна в момента. Опитайте по-късно.");
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
            return new RentalOperationResult(false, "Услугата за наеми е недостъпна в момента. Опитайте по-късно.");
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
            return new RentalOperationResult(false, "Услугата за наеми е недостъпна в момента. Опитайте по-късно.");
        }
    }
}
