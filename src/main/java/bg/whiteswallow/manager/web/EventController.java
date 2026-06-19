package bg.whiteswallow.manager.web;

import bg.whiteswallow.manager.model.dto.event.EventAddDTO;
import bg.whiteswallow.manager.service.EventService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @ModelAttribute("eventAddDTO")
    public EventAddDTO initEventAddDTO() {
        return new EventAddDTO();
    }

    @GetMapping
    public String events(Model model) {
        model.addAttribute("allEvents", eventService.getAllEvents());
        return "events";
    }

    @GetMapping("/add")
    public String addEvent(HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) {
            return "redirect:/events";
        }
        return "event-add";
    }

    @PostMapping("/add")
    public String confirmAddEvent(@Valid EventAddDTO eventAddDTO,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {

        if (!"ADMIN".equals(session.getAttribute("user_role"))) {
            return "redirect:/events";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("eventAddDTO", eventAddDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.eventAddDTO", bindingResult);
            return "redirect:/events/add";
        }

        eventService.addEvent(eventAddDTO);
        return "redirect:/events";
    }

    @PostMapping("/delete/{id}")
    public String deleteEvent(@PathVariable UUID id, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) {
            return "redirect:/events";
        }
        eventService.deleteEvent(id);
        return "redirect:/events";
    }

    @GetMapping("/edit/{id}")
    public String editEvent(@PathVariable UUID id, Model model, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("user_role"))) {
            return "redirect:/events";
        }

        if (!model.containsAttribute("eventEditDTO")) {
            model.addAttribute("eventEditDTO", eventService.getEventForEdit(id));
        }
        model.addAttribute("eventId", id);
        return "event-edit";
    }

    @PostMapping("/edit/{id}")
    public String confirmEditEvent(@PathVariable UUID id,
                                   @Valid @ModelAttribute("eventEditDTO") EventAddDTO eventEditDTO,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {

        if (!"ADMIN".equals(session.getAttribute("user_role"))) {
            return "redirect:/events";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("eventEditDTO", eventEditDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.eventEditDTO", bindingResult);
            return "redirect:/events/edit/" + id;
        }

        eventService.updateEvent(id, eventEditDTO);
        return "redirect:/events";
    }
}