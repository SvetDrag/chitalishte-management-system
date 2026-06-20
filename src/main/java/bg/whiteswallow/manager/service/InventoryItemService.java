package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.dto.inventory.InventoryItemAddDTO;
import bg.whiteswallow.manager.model.entity.inventory.InventoryItem;

import java.util.List;
import java.util.UUID;

public interface InventoryItemService {
    void addItem(InventoryItemAddDTO itemAddDTO);
    List<InventoryItem> getAllItems();
    void deleteItem(UUID id);
    InventoryItemAddDTO getItemForEdit(UUID id);
    void updateItem(UUID id, InventoryItemAddDTO itemDTO);
}