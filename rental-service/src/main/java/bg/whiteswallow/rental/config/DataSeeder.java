package bg.whiteswallow.rental.config;

import bg.whiteswallow.rental.entity.EquipmentItem;
import bg.whiteswallow.rental.entity.Hall;
import bg.whiteswallow.rental.entity.RentalRequest;
import bg.whiteswallow.rental.entity.RentalStatus;
import bg.whiteswallow.rental.repository.EquipmentItemRepository;
import bg.whiteswallow.rental.repository.HallRepository;
import bg.whiteswallow.rental.repository.RentalRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final HallRepository hallRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final RentalRequestRepository rentalRequestRepository;

    public DataSeeder(HallRepository hallRepository, EquipmentItemRepository equipmentItemRepository,
                       RentalRequestRepository rentalRequestRepository) {
        this.hallRepository = hallRepository;
        this.equipmentItemRepository = equipmentItemRepository;
        this.rentalRequestRepository = rentalRequestRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Seeding rental-service database with demo data...");
        wipe();

        List<Hall> halls = seedHalls();
        List<EquipmentItem> equipment = seedEquipment();
        seedRentalRequests(halls, equipment);

        log.info("Rental-service seeding complete.");
    }

    private void wipe() {
        rentalRequestRepository.deleteAll();
        equipmentItemRepository.deleteAll();
        hallRepository.deleteAll();
        hallRepository.flush();
    }

    private List<Hall> seedHalls() {
        return hallRepository.saveAll(List.of(
                Hall.builder().name("Голяма зала").capacity(200)
                        .description("Основна зала за концерти и тържества.").build(),
                Hall.builder().name("Малка зала").capacity(50)
                        .description("Подходяща за срещи, семинари и по-камерни събития.").build(),
                Hall.builder().name("Конферентна зала").capacity(30)
                        .description("Зала с проектор и озвучаване за конференции.").build()
        ));
    }

    private List<EquipmentItem> seedEquipment() {
        return equipmentItemRepository.saveAll(List.of(
                EquipmentItem.builder().name("Проектор Epson").pricePerRental(new BigDecimal("40.00")).available(true).build(),
                EquipmentItem.builder().name("Озвучителна система").pricePerRental(new BigDecimal("120.00")).available(true).build(),
                EquipmentItem.builder().name("Сценично осветление").pricePerRental(new BigDecimal("90.00")).available(true).build(),
                EquipmentItem.builder().name("Комплект столове (100 бр.)").pricePerRental(new BigDecimal("60.00")).available(true).build(),
                EquipmentItem.builder().name("Безжични микрофони (2 бр.)").pricePerRental(new BigDecimal("35.00")).available(false).build()
        ));
    }

    private void seedRentalRequests(List<Hall> halls, List<EquipmentItem> equipment) {
        Hall bigHall = halls.get(0);
        Hall smallHall = halls.get(1);
        Hall confHall = halls.get(2);

        EquipmentItem projector = equipment.get(0);
        EquipmentItem sound = equipment.get(1);
        EquipmentItem lighting = equipment.get(2);
        EquipmentItem chairs = equipment.get(3);
        EquipmentItem mics = equipment.get(4);

        LocalDate today = LocalDate.now();
        LocalDate twoMonthsAgo = today.minusMonths(2);
        LocalDate oneMonthAgo = today.minusMonths(1);
        LocalDate nextMonth = today.plusMonths(1);

        List<RentalRequest> requests = List.of(
                request("Сдружение \"Извор\"", "0888123456", "izvor@example.com",
                        twoMonthsAgo.withDayOfMonth(14).atTime(18, 0), twoMonthsAgo.withDayOfMonth(14).atTime(23, 0),
                        "Годишен благотворителен бал", RentalStatus.COMPLETED, new BigDecimal("450.00"),
                        bigHall, List.of(sound, lighting, chairs)),

                request("Иван Колев", "0888234567", "ivan.kolev@example.com",
                        twoMonthsAgo.withDayOfMonth(22).atTime(12, 0), twoMonthsAgo.withDayOfMonth(22).atTime(18, 0),
                        "Рожден ден", RentalStatus.COMPLETED, new BigDecimal("180.00"),
                        smallHall, List.of(sound)),

                request("ТП \"Знание\" ЕООД", "0888345678", "office@znanie-example.bg",
                        twoMonthsAgo.withDayOfMonth(28).atTime(9, 0), twoMonthsAgo.withDayOfMonth(28).atTime(17, 0),
                        "Бизнес семинар", RentalStatus.CANCELLED, new BigDecimal("160.00"),
                        confHall, List.of(projector)),

                request("Мария Тодорова", "0888456789", "maria.todorova@example.com",
                        oneMonthAgo.withDayOfMonth(6).atTime(17, 0), oneMonthAgo.withDayOfMonth(6).atTime(22, 0),
                        "Сватбено тържество", RentalStatus.COMPLETED, new BigDecimal("520.00"),
                        bigHall, List.of(sound, lighting, chairs, mics)),

                request("НЧ \"Пробуда\" - партньорско събитие", "0888567890", "probuda@example.com",
                        oneMonthAgo.withDayOfMonth(19).atTime(18, 30), oneMonthAgo.withDayOfMonth(19).atTime(21, 0),
                        "Съвместен концерт", RentalStatus.COMPLETED, new BigDecimal("300.00"),
                        bigHall, List.of(sound, lighting)),

                request("Стефан Русев", "0888678901", "stefan.rusev@example.com",
                        oneMonthAgo.withDayOfMonth(25).atTime(10, 0), oneMonthAgo.withDayOfMonth(25).atTime(13, 0),
                        "Детски рожден ден", RentalStatus.CANCELLED, new BigDecimal("120.00"),
                        smallHall, List.of()),

                request("Фирма \"Хоризонт\" АД", "0888789012", "events@horizont-example.bg",
                        today.withDayOfMonth(Math.min(1, today.lengthOfMonth())).atTime(9, 0),
                        today.withDayOfMonth(Math.min(1, today.lengthOfMonth())).atTime(17, 0),
                        "Годишна конференция", RentalStatus.COMPLETED, new BigDecimal("250.00"),
                        confHall, List.of(projector, mics)),

                request("Военно-патриотичен клуб \"Родина\"", "0888890123", "rodina@example.com",
                        today.withDayOfMonth(Math.min(12, today.lengthOfMonth())).atTime(17, 0),
                        today.withDayOfMonth(Math.min(12, today.lengthOfMonth())).atTime(20, 0),
                        "Тържествено събрание", RentalStatus.CONFIRMED, new BigDecimal("220.00"),
                        smallHall, List.of(sound)),

                request("Елена Петкова", "0888901234", "elena.petkova@example.com",
                        today.withDayOfMonth(Math.min(20, today.lengthOfMonth())).atTime(18, 0),
                        today.withDayOfMonth(Math.min(20, today.lengthOfMonth())).atTime(23, 30),
                        "Абитуриентски бал", RentalStatus.PENDING, new BigDecimal("480.00"),
                        bigHall, List.of(sound, lighting, chairs)),

                request("Клуб по танци \"Ритъм\"", "0889012345", "ritam@example.com",
                        nextMonth.withDayOfMonth(3).atTime(18, 0), nextMonth.withDayOfMonth(3).atTime(21, 0),
                        "Отчетен танцов спектакъл", RentalStatus.CONFIRMED, new BigDecimal("260.00"),
                        bigHall, List.of(sound, lighting)),

                request("Георги Николов", "0889123456", "georgi.nikolov@example.com",
                        nextMonth.withDayOfMonth(11).atTime(11, 0), nextMonth.withDayOfMonth(11).atTime(14, 0),
                        "Кръщене", RentalStatus.PENDING, new BigDecimal("190.00"),
                        smallHall, List.of()),

                request("Сдружение на пенсионера \"Заедно\"", "0889234567", "zaedno@example.com",
                        nextMonth.withDayOfMonth(18).atTime(9, 0), nextMonth.withDayOfMonth(18).atTime(12, 0),
                        "Информационна среща", RentalStatus.PENDING, new BigDecimal("100.00"),
                        confHall, List.of(projector))
        );

        rentalRequestRepository.saveAll(requests);
    }

    private RentalRequest request(String renterName, String renterPhone, String renterEmail,
                                   LocalDateTime start, LocalDateTime end, String purpose, RentalStatus status,
                                   BigDecimal price, Hall hall, List<EquipmentItem> equipmentItems) {
        return RentalRequest.builder()
                .renterName(renterName)
                .renterPhone(renterPhone)
                .renterEmail(renterEmail)
                .startDateTime(start)
                .endDateTime(end)
                .purpose(purpose)
                .status(status)
                .price(price)
                .createdOn(start.minusDays(10))
                .hall(hall)
                .equipmentItems(equipmentItems)
                .build();
    }
}
