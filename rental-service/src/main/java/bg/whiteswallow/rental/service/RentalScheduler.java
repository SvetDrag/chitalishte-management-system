package bg.whiteswallow.rental.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RentalScheduler {

    private final RentalRequestService rentalRequestService;

    public RentalScheduler(RentalRequestService rentalRequestService) {
        this.rentalRequestService = rentalRequestService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void completeExpiredRentals() {
        rentalRequestService.completeExpiredRentals();
    }
}
