package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.inventory.InventoryItemAddDTO;
import bg.whiteswallow.manager.model.entity.inventory.InventoryItem;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.repository.InventoryItemRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.InventoryItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;

    public InventoryItemServiceImpl(InventoryItemRepository inventoryItemRepository, UserRepository userRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void addItem(InventoryItemAddDTO itemAddDTO) {
        InventoryItem item = InventoryItem.builder()
                .name(itemAddDTO.getName())
                .itemCondition(itemAddDTO.getItemCondition())
                .status(itemAddDTO.getStatus())
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

    @Override
    public void lendItem(UUID itemId, UUID userId) {
        InventoryItem item = inventoryItemRepository.findById(itemId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        item.setStatus(bg.whiteswallow.manager.model.entity.inventory.ItemStatus.BORROWED);
        item.setBorrowedBy(user);
        inventoryItemRepository.save(item);
    }

    @Override
    public void returnItem(UUID itemId) {
        InventoryItem item = inventoryItemRepository.findById(itemId).orElseThrow();

        item.setStatus(bg.whiteswallow.manager.model.entity.inventory.ItemStatus.AVAILABLE);
        item.setBorrowedBy(null);
        inventoryItemRepository.save(item);
    }
}