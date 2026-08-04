package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.inventory.InventoryItemAddDTO;
import bg.whiteswallow.manager.model.entity.inventory.InventoryItem;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.exception.ResourceNotFoundException;
import bg.whiteswallow.manager.repository.InventoryItemRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.InventoryItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryItemServiceImpl implements InventoryItemService {

    private static final Logger log = LoggerFactory.getLogger(InventoryItemServiceImpl.class);

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
        log.info("Added inventory item '{}'", item.getName());
    }

    @Override
    public List<InventoryItem> getAllItems() {
        return inventoryItemRepository.findAll();
    }

    @Override
    public void deleteItem(UUID id) {
        inventoryItemRepository.deleteById(id);
        log.info("Deleted inventory item {}", id);
    }

    @Override
    public InventoryItemAddDTO getItemForEdit(UUID id) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Инвентарната вещ не е намерена."));
        InventoryItemAddDTO dto = new InventoryItemAddDTO();
        dto.setName(item.getName());
        dto.setItemCondition(item.getItemCondition());
        dto.setStatus(item.getStatus());
        return dto;
    }

    @Override
    public void updateItem(UUID id, InventoryItemAddDTO itemDTO) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Инвентарната вещ не е намерена."));
        item.setName(itemDTO.getName());
        item.setItemCondition(itemDTO.getItemCondition());
        item.setStatus(itemDTO.getStatus());
        inventoryItemRepository.save(item);
        log.info("Updated inventory item {}", id);
    }

    @Override
    public void lendItem(UUID itemId, UUID userId) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Инвентарната вещ не е намерена."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Потребителят не е намерен."));

        item.setStatus(bg.whiteswallow.manager.model.entity.inventory.ItemStatus.BORROWED);
        item.setBorrowedBy(user);
        item.setBorrowedOn(LocalDateTime.now());
        inventoryItemRepository.save(item);
        log.info("Item '{}' lent to user '{}'", item.getName(), user.getUsername());
    }

    @Override
    public void returnItem(UUID itemId) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Инвентарната вещ не е намерена."));

        item.setStatus(bg.whiteswallow.manager.model.entity.inventory.ItemStatus.AVAILABLE);
        item.setBorrowedBy(null);
        item.setBorrowedOn(null);
        inventoryItemRepository.save(item);
        log.info("Item '{}' returned", item.getName());
    }

    @Override
    public List<InventoryItem> getOverdueItems(int overdueAfterDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(overdueAfterDays);
        return inventoryItemRepository.findAllByStatusAndBorrowedOnBefore(
                bg.whiteswallow.manager.model.entity.inventory.ItemStatus.BORROWED, threshold);
    }

    @Override
    public void flagOverdueItems(int overdueAfterDays) {
        List<InventoryItem> overdueItems = getOverdueItems(overdueAfterDays);

        for (InventoryItem item : overdueItems) {
            item.setStatus(bg.whiteswallow.manager.model.entity.inventory.ItemStatus.OVERDUE);
            inventoryItemRepository.save(item);
            log.warn("Inventory item '{}' flagged OVERDUE - borrowed by '{}' for more than {} days",
                    item.getName(), item.getBorrowedBy().getUsername(), overdueAfterDays);
        }
    }
}