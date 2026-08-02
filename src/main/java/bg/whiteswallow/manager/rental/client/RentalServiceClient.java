package bg.whiteswallow.manager.rental.client;

import bg.whiteswallow.manager.rental.dto.EquipmentItemCreateDTO;
import bg.whiteswallow.manager.rental.dto.EquipmentItemResponseDTO;
import bg.whiteswallow.manager.rental.dto.HallCreateDTO;
import bg.whiteswallow.manager.rental.dto.HallResponseDTO;
import bg.whiteswallow.manager.rental.dto.RentalRequestCreateDTO;
import bg.whiteswallow.manager.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.manager.rental.dto.RentalStatusUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "rental-service", url = "${rental-service.url}")
public interface RentalServiceClient {

    @GetMapping("/api/rentals")
    List<RentalRequestResponseDTO> getAllRentalRequests();

    @PostMapping("/api/rentals")
    RentalRequestResponseDTO createRentalRequest(@RequestBody RentalRequestCreateDTO createDTO);

    @PutMapping("/api/rentals/{id}/status")
    RentalRequestResponseDTO updateStatus(@PathVariable("id") UUID id, @RequestBody RentalStatusUpdateDTO statusUpdateDTO);

    @DeleteMapping("/api/rentals/{id}")
    void deleteRentalRequest(@PathVariable("id") UUID id);

    @GetMapping("/api/halls")
    List<HallResponseDTO> getAllHalls();

    @PostMapping("/api/halls")
    HallResponseDTO createHall(@RequestBody HallCreateDTO createDTO);

    @PutMapping("/api/halls/{id}")
    HallResponseDTO updateHall(@PathVariable("id") UUID id, @RequestBody HallCreateDTO updateDTO);

    @DeleteMapping("/api/halls/{id}")
    void deleteHall(@PathVariable("id") UUID id);

    @GetMapping("/api/equipment")
    List<EquipmentItemResponseDTO> getAllEquipmentItems();

    @PostMapping("/api/equipment")
    EquipmentItemResponseDTO createEquipmentItem(@RequestBody EquipmentItemCreateDTO createDTO);

    @PutMapping("/api/equipment/{id}")
    EquipmentItemResponseDTO updateEquipmentItem(@PathVariable("id") UUID id, @RequestBody EquipmentItemCreateDTO updateDTO);

    @DeleteMapping("/api/equipment/{id}")
    void deleteEquipmentItem(@PathVariable("id") UUID id);
}
