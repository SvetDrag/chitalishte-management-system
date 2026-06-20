package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.dto.inventory.InventoryItemAddDTO;
import bg.whiteswallow.manager.model.entity.inventory.ItemStatus;
import bg.whiteswallow.manager.service.InventoryItemService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryItemService inventoryItemService;

    public InventoryController(InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    @ModelAttribute("inventoryItemAddDTO")
    public InventoryItemAddDTO initDTO() {
        return new InventoryItemAddDTO();
    }

    @ModelAttribute("itemStatuses")
    public ItemStatus[] itemStatuses() {
        return ItemStatus.values();
    }

    @GetMapping
    public String inventory(Model model) {
        model.addAttribute("allItems", inventoryItemService.getAllItems());
        return "inventory";
    }

    @GetMapping("/add")
    public String addItem(HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/inventory";
        return "inventory-add";
    }

    @PostMapping("/add")
    public String confirmAddItem(@Valid InventoryItemAddDTO inventoryItemAddDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/inventory";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("inventoryItemAddDTO", inventoryItemAddDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.inventoryItemAddDTO", bindingResult);
            return "redirect:/inventory/add";
        }

        inventoryItemService.addItem(inventoryItemAddDTO);
        return "redirect:/inventory";
    }

    @PostMapping("/delete/{id}")
    public String deleteItem(@PathVariable UUID id, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/inventory";
        inventoryItemService.deleteItem(id);
        return "redirect:/inventory";
    }

    @GetMapping("/edit/{id}")
    public String editItem(@PathVariable UUID id, Model model, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/inventory";
        if (!model.containsAttribute("inventoryEditDTO")) {
            model.addAttribute("inventoryEditDTO", inventoryItemService.getItemForEdit(id));
        }
        model.addAttribute("itemId", id);
        return "inventory-edit";
    }

    @PostMapping("/edit/{id}")
    public String confirmEditItem(@PathVariable UUID id,
                                  @Valid @ModelAttribute("inventoryEditDTO") InventoryItemAddDTO inventoryEditDTO,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) return "redirect:/inventory";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("inventoryEditDTO", inventoryEditDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.inventoryEditDTO", bindingResult);
            return "redirect:/inventory/edit/" + id;
        }

        inventoryItemService.updateItem(id, inventoryEditDTO);
        return "redirect:/inventory";
    }
}