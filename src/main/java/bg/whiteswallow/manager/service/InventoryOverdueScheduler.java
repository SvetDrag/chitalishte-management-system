package bg.whiteswallow.manager.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InventoryOverdueScheduler {

    private static final int OVERDUE_AFTER_DAYS = 14;

    private final InventoryItemService inventoryItemService;

    public InventoryOverdueScheduler(InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000)
    public void flagOverdueItems() {
        inventoryItemService.flagOverdueItems(OVERDUE_AFTER_DAYS);
    }
}
