package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.entity.inventory.InventoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryOverdueScheduler {

    private static final Logger log = LoggerFactory.getLogger(InventoryOverdueScheduler.class);
    private static final int OVERDUE_AFTER_DAYS = 14;

    private final InventoryItemService inventoryItemService;

    public InventoryOverdueScheduler(InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000)
    public void logOverdueItems() {
        List<InventoryItem> overdueItems = inventoryItemService.getOverdueItems(OVERDUE_AFTER_DAYS);

        for (InventoryItem item : overdueItems) {
            log.warn("Inventory item '{}' has been borrowed by '{}' for more than {} days",
                    item.getName(), item.getBorrowedBy().getUsername(), OVERDUE_AFTER_DAYS);
        }
    }
}
