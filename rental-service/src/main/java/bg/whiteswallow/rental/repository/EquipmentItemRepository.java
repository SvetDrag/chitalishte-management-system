package bg.whiteswallow.rental.repository;

import bg.whiteswallow.rental.entity.EquipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EquipmentItemRepository extends JpaRepository<EquipmentItem, UUID> {
}
