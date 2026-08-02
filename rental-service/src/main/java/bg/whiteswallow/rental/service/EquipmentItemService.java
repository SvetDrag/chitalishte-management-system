package bg.whiteswallow.rental.service;

import bg.whiteswallow.rental.dto.EquipmentItemCreateDTO;
import bg.whiteswallow.rental.dto.EquipmentItemResponseDTO;

import java.util.List;
import java.util.UUID;

public interface EquipmentItemService {
    EquipmentItemResponseDTO createEquipmentItem(EquipmentItemCreateDTO createDTO);
    EquipmentItemResponseDTO updateEquipmentItem(UUID id, EquipmentItemCreateDTO updateDTO);
    void deleteEquipmentItem(UUID id);
    List<EquipmentItemResponseDTO> getAllEquipmentItems();
}
