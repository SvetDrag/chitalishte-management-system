package bg.whiteswallow.manager.config;

import bg.whiteswallow.manager.model.entity.course.Course;
import bg.whiteswallow.manager.model.entity.course.CourseType;
import bg.whiteswallow.manager.model.entity.course.LessonAttendance;
import bg.whiteswallow.manager.model.entity.course.LessonSlot;
import bg.whiteswallow.manager.model.entity.event.Event;
import bg.whiteswallow.manager.model.entity.inventory.InventoryItem;
import bg.whiteswallow.manager.model.entity.inventory.ItemStatus;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.model.entity.user.UserRole;
import bg.whiteswallow.manager.repository.CourseRepository;
import bg.whiteswallow.manager.repository.EventRepository;
import bg.whiteswallow.manager.repository.InventoryItemRepository;
import bg.whiteswallow.manager.repository.LessonAttendanceRepository;
import bg.whiteswallow.manager.repository.LessonSlotRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonSlotRepository lessonSlotRepository;
    private final LessonAttendanceRepository lessonAttendanceRepository;
    private final EventRepository eventRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, CourseRepository courseRepository,
                       LessonSlotRepository lessonSlotRepository, LessonAttendanceRepository lessonAttendanceRepository,
                       EventRepository eventRepository, InventoryItemRepository inventoryItemRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.lessonSlotRepository = lessonSlotRepository;
        this.lessonAttendanceRepository = lessonAttendanceRepository;
        this.eventRepository = eventRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Seeding main-app database with demo data...");
        wipe();

        List<User> employees = seedUsers();
        User instructorDance = employees.get(1);
        User instructorPiano = employees.get(2);
        User instructorGuitar = employees.get(3);
        List<User> regularUsers = employees.subList(4, employees.size());

        List<Course> courses = seedCourses(instructorDance, instructorPiano, instructorGuitar);
        seedSchedule(courses, regularUsers);
        seedEvents();
        seedInventory(regularUsers);

        log.info("Main-app seeding complete.");
    }

    private void wipe() {
        lessonAttendanceRepository.deleteAll();
        lessonSlotRepository.deleteAll();
        courseRepository.deleteAll();
        eventRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.flush();
    }

    private List<User> seedUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<User> users = new ArrayList<>();

        users.add(buildUser("admin", "admin@chitalishte.bg", "Admin123!", "Админ", "Админов", UserRole.ADMIN, now));

        users.add(buildUser("petar.ivanov", "petar.ivanov@chitalishte.bg", "Parola123!", "Петър", "Иванов", UserRole.EMPLOYEE, now));
        users.add(buildUser("maria.georgieva", "maria.georgieva@chitalishte.bg", "Parola123!", "Мария", "Георгиева", UserRole.EMPLOYEE, now));
        users.add(buildUser("ivan.petrov", "ivan.petrov@chitalishte.bg", "Parola123!", "Иван", "Петров", UserRole.EMPLOYEE, now));

        users.add(buildUser("georgi.dimitrov", "georgi.dimitrov@example.com", "Parola123!", "Георги", "Димитров", UserRole.USER, now));
        users.add(buildUser("elena.stoyanova", "elena.stoyanova@example.com", "Parola123!", "Елена", "Стоянова", UserRole.USER, now));
        users.add(buildUser("stefan.nikolov", "stefan.nikolov@example.com", "Parola123!", "Стефан", "Николов", UserRole.USER, now));
        users.add(buildUser("victoria.hristova", "victoria.hristova@example.com", "Parola123!", "Виктория", "Христова", UserRole.USER, now));
        users.add(buildUser("kristian.angelov", "kristian.angelov@example.com", "Parola123!", "Кристиан", "Ангелов", UserRole.USER, now));
        users.add(buildUser("teodora.vasileva", "teodora.vasileva@example.com", "Parola123!", "Теодора", "Василева", UserRole.USER, now));

        return userRepository.saveAll(users);
    }

    private User buildUser(String username, String email, String rawPassword, String firstName, String lastName,
                            UserRole role, LocalDateTime now) {
        return User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .isActive(true)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    private List<Course> seedCourses(User instructorDance, User instructorPiano, User instructorGuitar) {
        List<Course> courses = new ArrayList<>();

        courses.add(Course.builder()
                .name("Народни танци")
                .groupPricePerLesson(new BigDecimal("15.00"))
                .instructor(instructorDance)
                .build());

        courses.add(Course.builder()
                .name("Пиано")
                .individualPricePerLesson(new BigDecimal("25.00"))
                .instructor(instructorPiano)
                .build());

        courses.add(Course.builder()
                .name("Вокална група")
                .groupPricePerLesson(new BigDecimal("10.00"))
                .instructor(instructorPiano)
                .build());

        courses.add(Course.builder()
                .name("Класическа китара")
                .groupPricePerLesson(new BigDecimal("12.00"))
                .individualPricePerLesson(new BigDecimal("20.00"))
                .instructor(instructorGuitar)
                .build());

        return courseRepository.saveAll(courses);
    }

    private void seedSchedule(List<Course> courses, List<User> regularUsers) {
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsAgoStart = today.minusMonths(2).withDayOfMonth(1);
        LocalDate oneMonthAgoStart = today.minusMonths(1).withDayOfMonth(1);
        LocalDate thisMonthStart = today.withDayOfMonth(1);
        LocalDate nextMonthStart = today.plusMonths(1).withDayOfMonth(1);

        int[] dayOffsets = {2, 9, 16, 23};
        LocalTime[] times = {LocalTime.of(17, 0), LocalTime.of(18, 30)};

        int userCursor = 0;
        for (int c = 0; c < courses.size(); c++) {
            Course course = courses.get(c);
            CourseType type = course.offers(CourseType.GROUP) ? CourseType.GROUP : CourseType.INDIVIDUAL;
            LocalTime time = times[c % times.length];

            userCursor = seedMonthSlots(course, type, twoMonthsAgoStart, dayOffsets, time, regularUsers, userCursor);
            userCursor = seedMonthSlots(course, type, oneMonthAgoStart, dayOffsets, time, regularUsers, userCursor);
            userCursor = seedMonthSlots(course, type, thisMonthStart, dayOffsets, time, regularUsers, userCursor);
            userCursor = seedMonthSlots(course, type, nextMonthStart, dayOffsets, time, regularUsers, userCursor);
        }
    }

    private int seedMonthSlots(Course course, CourseType type, LocalDate monthStart, int[] dayOffsets, LocalTime time,
                                List<User> regularUsers, int userCursor) {
        List<LessonSlot> monthSlots = new ArrayList<>();

        for (int dayOffset : dayOffsets) {
            LocalDate slotDate = monthStart.plusDays(dayOffset - 1);
            LocalDateTime startTime = slotDate.atTime(time);

            List<User> enrolled = new ArrayList<>();
            int enrollCount = 2 + (dayOffset % 3);
            for (int i = 0; i < enrollCount; i++) {
                enrolled.add(regularUsers.get(userCursor % regularUsers.size()));
                userCursor++;
            }

            LessonSlot slot = LessonSlot.builder()
                    .course(course)
                    .startTime(startTime)
                    .type(type)
                    .maxCapacity(8)
                    .enrolledUsers(enrolled)
                    .build();
            monthSlots.add(lessonSlotRepository.save(slot));
        }

        int lastPastSlotIndex = -1;
        for (int i = 0; i < monthSlots.size(); i++) {
            if (!monthSlots.get(i).getStartTime().isAfter(LocalDateTime.now())) {
                lastPastSlotIndex = i;
            }
        }

        for (int i = 0; i <= lastPastSlotIndex; i++) {
            LessonSlot slot = monthSlots.get(i);
            boolean leaveLastEnrolledUserUnmarked = (i == lastPastSlotIndex);
            List<User> enrolled = slot.getEnrolledUsers();
            for (int u = 0; u < enrolled.size(); u++) {
                if (leaveLastEnrolledUserUnmarked && u == enrolled.size() - 1) {
                    continue;
                }
                boolean paid = (u % 3) != 0;
                LessonAttendance attendance = LessonAttendance.builder()
                        .user(enrolled.get(u))
                        .course(course)
                        .lessonSlot(slot)
                        .date(slot.getStartTime().toLocalDate())
                        .isPaid(paid)
                        .build();
                lessonAttendanceRepository.save(attendance);
            }
        }

        return userCursor;
    }

    private void seedEvents() {
        LocalDate today = LocalDate.now();
        List<Event> events = new ArrayList<>();

        events.add(buildEvent("Патронен празник на читалището", "Тържествен концерт по случай патронния празник.",
                today.minusMonths(2).withDayOfMonth(20).atTime(18, 0), "Голяма зала"));
        events.add(buildEvent("Изложба на детското ателие", "Изложба на рисунки от учениците в ателие \"Рисуване\".",
                today.minusMonths(1).withDayOfMonth(10).atTime(17, 0), "Фоайе"));
        events.add(buildEvent("Лятна вечер на народните танци", "Открит концерт на танцов състав \"Извор\".",
                today.minusMonths(1).withDayOfMonth(27).atTime(19, 0), "Летен театър"));
        events.add(buildEvent("Ден на отворените врати", "Представяне на всички школи и групи в читалището.",
                today.withDayOfMonth(Math.min(15, today.lengthOfMonth())).atTime(10, 0), "Голяма зала"));
        events.add(buildEvent("Есенен благотворителен концерт", "Благотворителен концерт в подкрепа на детски дом.",
                today.plusMonths(1).withDayOfMonth(5).atTime(18, 30), "Голяма зала"));

        eventRepository.saveAll(events);
    }

    private Event buildEvent(String title, String description, LocalDateTime eventDate, String location) {
        return Event.builder()
                .title(title)
                .description(description)
                .eventDate(eventDate)
                .location(location)
                .build();
    }

    private void seedInventory(List<User> regularUsers) {
        LocalDateTime now = LocalDateTime.now();
        List<InventoryItem> items = new ArrayList<>();

        items.add(InventoryItem.builder().name("Акустична китара Yamaha")
                .itemCondition("Добро").status(ItemStatus.BORROWED)
                .borrowedBy(regularUsers.get(0)).borrowedOn(now.minusDays(20)).build());

        items.add(InventoryItem.builder().name("Народна носия - женска")
                .itemCondition("Много добро").status(ItemStatus.BORROWED)
                .borrowedBy(regularUsers.get(1)).borrowedOn(now.minusDays(5)).build());

        items.add(InventoryItem.builder().name("Народна носия - мъжка")
                .itemCondition("Добро").status(ItemStatus.AVAILABLE).build());

        items.add(InventoryItem.builder().name("Електронен синтезатор Casio")
                .itemCondition("Отлично").status(ItemStatus.AVAILABLE).build());

        items.add(InventoryItem.builder().name("Комплект барабани")
                .itemCondition("Задоволително").status(ItemStatus.MAINTENANCE).build());

        items.add(InventoryItem.builder().name("Микрофон Shure SM58")
                .itemCondition("Отлично").status(ItemStatus.BORROWED)
                .borrowedBy(regularUsers.get(2)).borrowedOn(now.minusDays(45)).build());

        items.add(InventoryItem.builder().name("Мек преносим пулт за озвучаване")
                .itemCondition("Добро").status(ItemStatus.AVAILABLE).build());

        inventoryItemRepository.saveAll(items);
    }
}
