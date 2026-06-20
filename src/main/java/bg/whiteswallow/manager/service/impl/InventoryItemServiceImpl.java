package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.inventory.InventoryItemAddDTO;
import bg.whiteswallow.manager.model.entity.inventory.InventoryItem;
import bg.whiteswallow.manager.repository.InventoryItemRepository;
import bg.whiteswallow.manager.service.InventoryItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryItemServiceImpl(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @Override
    public void addItem(InventoryItemAddDTO itemAddDTO) {
        InventoryItem item = InventoryItem.builder()
                .name(itemAddDTO.getName())
                .itemCondition(itemAddDTO.getItemCondition())
                .status(itemAddDTO.getStatus())
                // По подразбиране при създаване никой не го е заел (borrowedBy = null)
                .build();
        inventoryItemRepository.save(item);
    }

    @Override
    public List<InventoryItem> getAllItems() {
        return inventoryItemRepository.findAll();
    }

    @Override
    public void deleteItem(UUID id) {
        inventoryItemRepository.deleteById(id);
    }

    @Override
    public InventoryItemAddDTO getItemForEdit(UUID id) {
        InventoryItem item = inventoryItemRepository.findById(id).orElseThrow();
        InventoryItemAddDTO dto = new InventoryItemAddDTO();
        dto.setName(item.getName());
        dto.setItemCondition(item.getItemCondition());
        dto.setStatus(item.getStatus());
        return dto;
    }

    @Override
    public void updateItem(UUID id, InventoryItemAddDTO itemDTO) {
        InventoryItem item = inventoryItemRepository.findById(id).orElseThrow();
        item.setName(itemDTO.getName());
        item.setItemCondition(itemDTO.getItemCondition());
        item.setStatus(itemDTO.getStatus());
        inventoryItemRepository.save(item);
    }
}