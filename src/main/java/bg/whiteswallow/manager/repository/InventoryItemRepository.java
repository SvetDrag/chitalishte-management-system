package bg.whiteswallow.manager.repository;

import bg.whiteswallow.manager.model.entity.inventory.InventoryItem;
import bg.whiteswallow.manager.model.entity.inventory.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {
    List<InventoryItem> findAllByStatus(ItemStatus status);
}