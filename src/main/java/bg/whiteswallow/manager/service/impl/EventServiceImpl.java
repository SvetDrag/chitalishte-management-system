package bg.whiteswallow.manager.service.impl;

import bg.whiteswallow.manager.model.dto.event.EventAddDTO;
import bg.whiteswallow.manager.model.entity.event.Event;
import bg.whiteswallow.manager.repository.EventRepository;
import bg.whiteswallow.manager.service.EventService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void addEvent(EventAddDTO eventAddDTO) {
        Event event = Event.builder()
                .title(eventAddDTO.getTitle())
                .description(eventAddDTO.getDescription())
                .eventDate(eventAddDTO.getEventDate())
                .location(eventAddDTO.getLocation())
                .build();

        eventRepository.save(event);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public void deleteEvent(UUID id) {
        eventRepository.deleteById(id);
    }

    @Override
    public EventAddDTO getEventForEdit(UUID id) {
        Event event = eventRepository.findById(id).orElseThrow();
        EventAddDTO dto = new EventAddDTO();
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setLocation(event.getLocation());
        return dto;
    }

    @Override
    public void updateEvent(UUID id, EventAddDTO eventDTO) {
        Event event = eventRepository.findById(id).orElseThrow();
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setEventDate(eventDTO.getEventDate());
        event.setLocation(eventDTO.getLocation());

        eventRepository.save(event);
    }
}