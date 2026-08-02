package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.exception.ResourceNotFoundException;
import bg.whiteswallow.manager.model.dto.event.EventAddDTO;
import bg.whiteswallow.manager.model.entity.event.Event;
import bg.whiteswallow.manager.repository.EventRepository;
import bg.whiteswallow.manager.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private EventAddDTO eventAddDTO;

    @BeforeEach
    void setUp() {
        eventAddDTO = new EventAddDTO();
        eventAddDTO.setTitle("Концерт");
        eventAddDTO.setDescription("Годишен концерт на читалището");
        eventAddDTO.setEventDate(LocalDateTime.now().plusDays(5));
        eventAddDTO.setLocation("Голяма зала");
    }

    @Test
    void addEvent_savesEventWithCorrectFields() {
        eventService.addEvent(eventAddDTO);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Концерт");
        assertThat(captor.getValue().getLocation()).isEqualTo("Голяма зала");
    }

    @Test
    void getAllEvents_returnsRepositoryResult() {
        List<Event> events = List.of(Event.builder().title("Event").build());
        when(eventRepository.findAll()).thenReturn(events);

        assertThat(eventService.getAllEvents()).isEqualTo(events);
    }

    @Test
    void deleteEvent_delegatesToRepository() {
        UUID id = UUID.randomUUID();

        eventService.deleteEvent(id);

        verify(eventRepository).deleteById(id);
    }

    @Test
    void getEventForEdit_mapsEntityToDto_whenFound() {
        UUID id = UUID.randomUUID();
        Event event = Event.builder().id(id).title("Стар текст").description("Описание").eventDate(LocalDateTime.now()).location("Място").build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        EventAddDTO result = eventService.getEventForEdit(id);

        assertThat(result.getTitle()).isEqualTo("Стар текст");
        assertThat(result.getLocation()).isEqualTo("Място");
    }

    @Test
    void getEventForEdit_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventForEdit(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateEvent_updatesFields_whenFound() {
        UUID id = UUID.randomUUID();
        Event event = Event.builder().id(id).title("Old").description("Old desc").eventDate(LocalDateTime.now()).location("Old loc").build();
        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        eventService.updateEvent(id, eventAddDTO);

        assertThat(event.getTitle()).isEqualTo("Концерт");
        assertThat(event.getLocation()).isEqualTo("Голяма зала");
        verify(eventRepository).save(event);
    }

    @Test
    void updateEvent_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(id, eventAddDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
