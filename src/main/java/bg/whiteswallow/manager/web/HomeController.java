package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.entity.course.LessonAttendance;
import bg.whiteswallow.manager.model.entity.user.UserRole;
import bg.whiteswallow.manager.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.manager.rental.dto.RentalStatus;
import bg.whiteswallow.manager.rental.service.RentalIntegrationService;
import bg.whiteswallow.manager.security.UserPrincipal;
import bg.whiteswallow.manager.service.DashboardService;
import bg.whiteswallow.manager.service.LessonAttendanceService;
import bg.whiteswallow.manager.service.LessonSlotService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Controller
public class HomeController {

    private final LessonSlotService lessonSlotService;
    private final LessonAttendanceService lessonAttendanceService;
    private final RentalIntegrationService rentalIntegrationService;
    private final DashboardService dashboardService;

    public HomeController(LessonSlotService lessonSlotService, LessonAttendanceService lessonAttendanceService,
                           RentalIntegrationService rentalIntegrationService, DashboardService dashboardService) {
        this.lessonSlotService = lessonSlotService;
        this.lessonAttendanceService = lessonAttendanceService;
        this.rentalIntegrationService = rentalIntegrationService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (principal != null) {
            return "redirect:/home";
        }
        model.addAttribute("publicDashboard", dashboardService.getPublicDashboard());
        return "index";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        UserRole role = principal.getUser().getRole();

        if (role == UserRole.USER) {
            model.addAttribute("myLessons", lessonSlotService.getUserUpcomingLessons(principal.getId()));
        } else if (role == UserRole.EMPLOYEE) {
            model.addAttribute("mySchedule", lessonSlotService.getInstructorSchedule(principal.getId()));
            model.addAttribute("instructorDashboard", dashboardService.getInstructorDashboard(principal.getId()));
        } else if (role == UserRole.ADMIN) {
            model.addAttribute("adminDashboard", dashboardService.getAdminDashboard());
        }

        return "home";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/admin/finances")
    public String showFinances(Model model) {
        List<LessonAttendance> allAttendances = lessonAttendanceService.getAllAttendances();
        allAttendances.sort(Comparator.comparing(LessonAttendance::getDate).reversed());

        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalUnpaid = BigDecimal.ZERO;

        for (LessonAttendance attendance : allAttendances) {
            BigDecimal price = attendance.getLessonSlot().getEffectivePrice();

            if (attendance.isPaid()) {
                totalPaid = totalPaid.add(price);
            } else {

                totalUnpaid = totalUnpaid.add(price);
            }
        }

        List<RentalRequestResponseDTO> allRentals = rentalIntegrationService.getAllRentalRequests();
        allRentals.sort(Comparator.comparing(RentalRequestResponseDTO::getStartDateTime).reversed());

        List<RentalRequestResponseDTO> billableRentals = allRentals.stream()
                .filter(r -> r.getStatus() != RentalStatus.CANCELLED)
                .toList();

        for (RentalRequestResponseDTO rental : billableRentals) {
            if (rental.getStatus() == RentalStatus.CONFIRMED || rental.getStatus() == RentalStatus.COMPLETED) {
                totalPaid = totalPaid.add(rental.getPrice());
            } else {
                totalUnpaid = totalUnpaid.add(rental.getPrice());
            }
        }

        model.addAttribute("attendances", allAttendances);
        model.addAttribute("rentals", billableRentals);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalUnpaid", totalUnpaid);

        return "admin-finances";
    }
}
