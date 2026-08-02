package bg.whiteswallow.manager.rental.web;

import bg.whiteswallow.manager.rental.dto.RentalRequestCreateDTO;
import bg.whiteswallow.manager.rental.dto.RentalStatus;
import bg.whiteswallow.manager.rental.service.RentalIntegrationService;
import bg.whiteswallow.manager.rental.service.RentalOperationResult;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/rentals")
public class RentalController {

    private final RentalIntegrationService rentalIntegrationService;

    public RentalController(RentalIntegrationService rentalIntegrationService) {
        this.rentalIntegrationService = rentalIntegrationService;
    }

    @ModelAttribute("rentalRequestCreateDTO")
    public RentalRequestCreateDTO initDTO() {
        return new RentalRequestCreateDTO();
    }

    @GetMapping
    public String rentals(Model model) {
        model.addAttribute("allRentals", rentalIntegrationService.getAllRentalRequests());
        return "admin-rentals";
    }

    @PostMapping("/add")
    public String confirmAddRental(@Valid RentalRequestCreateDTO rentalRequestCreateDTO,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("rentalRequestCreateDTO", rentalRequestCreateDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.rentalRequestCreateDTO", bindingResult);
            return "redirect:/admin/rentals";
        }

        RentalOperationResult result = rentalIntegrationService.createRentalRequest(rentalRequestCreateDTO);
        redirectAttributes.addFlashAttribute(result.success() ? "successMsg" : "errorMsg", result.message());
        return "redirect:/admin/rentals";
    }

    @PostMapping("/{id}/confirm")
    public String confirmRental(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        RentalOperationResult result = rentalIntegrationService.updateStatus(id, RentalStatus.CONFIRMED);
        redirectAttributes.addFlashAttribute(result.success() ? "successMsg" : "errorMsg", result.message());
        return "redirect:/admin/rentals";
    }

    @PostMapping("/{id}/cancel")
    public String cancelRental(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        RentalOperationResult result = rentalIntegrationService.updateStatus(id, RentalStatus.CANCELLED);
        redirectAttributes.addFlashAttribute(result.success() ? "successMsg" : "errorMsg", result.message());
        return "redirect:/admin/rentals";
    }

    @PostMapping("/{id}/delete")
    public String deleteRental(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        RentalOperationResult result = rentalIntegrationService.deleteRentalRequest(id);
        redirectAttributes.addFlashAttribute(result.success() ? "successMsg" : "errorMsg", result.message());
        return "redirect:/admin/rentals";
    }
}
