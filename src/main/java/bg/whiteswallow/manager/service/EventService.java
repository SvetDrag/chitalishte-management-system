package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.model.dto.event.EventAddDTO;
import bg.whiteswallow.manager.model.entity.event.Event;

import java.util.List;
import java.util.UUID;

public interface EventService {
    void addEvent(EventAddDTO eventAddDTO);
    List<Event> getAllEvents();
    void deleteEvent(UUID id);
    EventAddDTO getEventForEdit(UUID id);
    void updateEvent(UUID id, EventAddDTO eventDTO);
}