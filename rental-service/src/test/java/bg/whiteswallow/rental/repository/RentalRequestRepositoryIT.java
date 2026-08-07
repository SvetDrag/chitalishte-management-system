package bg.whiteswallow.rental.repository;

import bg.whiteswallow.rental.entity.Hall;
import bg.whiteswallow.rental.entity.RentalRequest;
import bg.whiteswallow.rental.entity.RentalStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(RentalRequestRepositoryIT.TestCacheConfig.class)
class RentalRequestRepositoryIT {

    @Autowired
    private RentalRequestRepository rentalRequestRepository;

    @Autowired
    private HallRepository hallRepository;

    private Hall saveHall(String name) {
        return hallRepository.save(Hall.builder().name(name).capacity(100).build());
    }

    private RentalRequest save(Hall hall, RentalStatus status, LocalDateTime start, LocalDateTime end) {
        RentalRequest rentalRequest = RentalRequest.builder()
                .hall(hall)
                .renterName("Renter")
                .renterPhone("0888000000")
                .renterEmail("renter@example.com")
                .startDateTime(start)
                .endDateTime(end)
                .purpose("Test purpose")
                .status(status)
                .price(new BigDecimal("100"))
                .createdOn(LocalDateTime.now())
                .build();
        return rentalRequestRepository.save(rentalRequest);
    }

    @Test
    void findAllByHallAndStatusInAndDateRange_findsOverlappingActiveRequestInSameHall() {
        Hall hall = saveHall("Голяма зала");
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(3);
        save(hall, RentalStatus.CONFIRMED, start, end);

        List<RentalRequest> overlapping = rentalRequestRepository
                .findAllByHallIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                        hall.getId(), List.of(RentalStatus.PENDING, RentalStatus.CONFIRMED), end, start);

        assertThat(overlapping).hasSize(1);
    }

    @Test
    void findAllByHallAndStatusInAndDateRange_ignoresOtherHalls() {
        Hall hallA = saveHall("Зала А");
        Hall hallB = saveHall("Зала Б");
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(3);
        save(hallA, RentalStatus.CONFIRMED, start, end);

        List<RentalRequest> overlapping = rentalRequestRepository
                .findAllByHallIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                        hallB.getId(), List.of(RentalStatus.PENDING, RentalStatus.CONFIRMED), end, start);

        assertThat(overlapping).isEmpty();
    }

    @Test
    void findAllByHallAndStatusInAndDateRange_ignoresCancelledRequests() {
        Hall hall = saveHall("Голяма зала");
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(3);
        save(hall, RentalStatus.CANCELLED, start, end);

        List<RentalRequest> overlapping = rentalRequestRepository
                .findAllByHallIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                        hall.getId(), List.of(RentalStatus.PENDING, RentalStatus.CONFIRMED), end, start);

        assertThat(overlapping).isEmpty();
    }

    @Test
    void findAllByStatusAndEndDateTimeBefore_findsOnlyExpiredConfirmedRequests() {
        Hall hall = saveHall("Голяма зала");
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        save(hall, RentalStatus.CONFIRMED, past.minusHours(3), past);
        save(hall, RentalStatus.CONFIRMED, future, future.plusHours(3));
        save(hall, RentalStatus.PENDING, past.minusHours(3), past);

        List<RentalRequest> expired = rentalRequestRepository
                .findAllByStatusAndEndDateTimeBefore(RentalStatus.CONFIRMED, LocalDateTime.now());

        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getEndDateTime()).isBefore(LocalDateTime.now());
    }

    @TestConfiguration
    static class TestCacheConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }
}
