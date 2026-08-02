package bg.whiteswallow.manager.rental.web;

import bg.whiteswallow.manager.rental.dto.EquipmentItemCreateDTO;
import bg.whiteswallow.manager.rental.dto.HallCreateDTO;
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

    @ModelAttribute("hallCreateDTO")
    public HallCreateDTO initHallDTO() {
        return new HallCreateDTO();
    }

    @ModelAttribute("equipmentItemCreateDTO")
    public EquipmentItemCreateDTO initEquipmentDTO() {
        return new EquipmentItemCreateDTO();
    }

    @GetMapping
    public String rentals(Model model) {
        model.addAttribute("allRentals", rentalIntegrationService.getAllRentalRequests());
        model.addAttribute("allHalls", rentalIntegrationService.getAllHalls());
        model.addAttribute("allEquipment", rentalIntegrationService.getAllEquipmentItems());
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

    @PostMapping("/halls/add")
    public String confirmAddHall(@Valid HallCreateDTO hallCreateDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("hallCreateDTO", hallCreateDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.hallCreateDTO", bindingResult);
            return "redirect:/admin/rentals";
        }

        RentalOperationResult result = rentalIntegrationService.createHall(hallCreateDTO);
        redirectAttributes.addFlashAttribute(result.success() ? "successMsg" : "errorMsg", result.message());
        return "redirect:/admin/rentals";
    }

    @PostMapping("/halls/{id}/delete")
    public String deleteHall(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        RentalOperationResult result = rentalIntegrationService.deleteHall(id);
        redirectAttributes.addFlashAttribute(result.success() ? "successMsg" : "errorMsg", result.message());
        return "redirect:/admin/rentals";
    }

    @PostMapping("/equipment/add")
    public String confirmAddEquipment(@Valid EquipmentItemCreateDTO equipmentItemCreateDTO,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("equipmentItemCreateDTO", equipmentItemCreateDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.equipmentItemCreateDTO", bindingResult);
            return "redirect:/admin/rentals";
        }

        RentalOperationResult result = rentalIntegrationService.createEquipmentItem(equipmentItemCreateDTO);
        redirectAttributes.addFlashAttribute(result.success() ? "successMsg" : "errorMsg", result.message());
        return "redirect:/admin/rentals";
    }

    @PostMapping("/equipment/{id}/delete")
    public String deleteEquipment(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        RentalOperationResult result = rentalIntegrationService.deleteEquipmentItem(id);
        redirectAttributes.addFlashAttribute(result.success() ? "successMsg" : "errorMsg", result.message());
        return "redirect:/admin/rentals";
    }
}
