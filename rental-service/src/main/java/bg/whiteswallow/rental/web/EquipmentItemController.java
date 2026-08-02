package bg.whiteswallow.rental.web;

import bg.whiteswallow.rental.dto.EquipmentItemCreateDTO;
import bg.whiteswallow.rental.dto.EquipmentItemResponseDTO;
import bg.whiteswallow.rental.service.EquipmentItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentItemController {

    private final EquipmentItemService equipmentItemService;

    public EquipmentItemController(EquipmentItemService equipmentItemService) {
        this.equipmentItemService = equipmentItemService;
    }

    @GetMapping
    public ResponseEntity<List<EquipmentItemResponseDTO>> getAllEquipmentItems() {
        return ResponseEntity.ok(equipmentItemService.getAllEquipmentItems());
    }

    @PostMapping
    public ResponseEntity<EquipmentItemResponseDTO> createEquipmentItem(@Valid @RequestBody EquipmentItemCreateDTO createDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentItemService.createEquipmentItem(createDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentItemResponseDTO> updateEquipmentItem(@PathVariable UUID id, @Valid @RequestBody EquipmentItemCreateDTO updateDTO) {
        return ResponseEntity.ok(equipmentItemService.updateEquipmentItem(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipmentItem(@PathVariable UUID id) {
        equipmentItemService.deleteEquipmentItem(id);
        return ResponseEntity.noContent().build();
    }
}
