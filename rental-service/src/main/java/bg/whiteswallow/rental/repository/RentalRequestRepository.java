package bg.whiteswallow.rental.repository;

import bg.whiteswallow.rental.entity.RentalRequest;
import bg.whiteswallow.rental.entity.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RentalRequestRepository extends JpaRepository<RentalRequest, UUID> {

    List<RentalRequest> findAllByStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
            List<RentalStatus> statuses, LocalDateTime endDateTime, LocalDateTime startDateTime);

    List<RentalRequest> findAllByStatusAndEndDateTimeBefore(RentalStatus status, LocalDateTime endDateTime);
}
