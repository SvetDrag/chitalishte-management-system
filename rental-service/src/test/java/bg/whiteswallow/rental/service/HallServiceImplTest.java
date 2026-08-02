package bg.whiteswallow.rental.service;

import bg.whiteswallow.rental.dto.HallCreateDTO;
import bg.whiteswallow.rental.dto.HallResponseDTO;
import bg.whiteswallow.rental.entity.Hall;
import bg.whiteswallow.rental.exception.ResourceNotFoundException;
import bg.whiteswallow.rental.repository.HallRepository;
import bg.whiteswallow.rental.service.impl.HallServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HallServiceImplTest {

    @Mock
    private HallRepository hallRepository;

    @InjectMocks
    private HallServiceImpl hallService;

    private HallCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        createDTO = new HallCreateDTO();
        createDTO.setName("Голяма зала");
        createDTO.setCapacity(200);
        createDTO.setDescription("Основна зала за тържества");
    }

    @Test
    void createHall_savesAndReturnsDto() {
        when(hallRepository.save(any(Hall.class))).thenAnswer(invocation -> {
            Hall hall = invocation.getArgument(0);
            hall.setId(UUID.randomUUID());
            return hall;
        });

        HallResponseDTO result = hallService.createHall(createDTO);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Голяма зала");
        assertThat(result.getCapacity()).isEqualTo(200);
    }

    @Test
    void updateHall_updatesAndReturnsDto_whenFound() {
        UUID id = UUID.randomUUID();
        Hall existing = Hall.builder().id(id).name("Стара зала").capacity(50).build();
        when(hallRepository.findById(id)).thenReturn(Optional.of(existing));
        when(hallRepository.save(any(Hall.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HallResponseDTO result = hallService.updateHall(id, createDTO);

        assertThat(result.getName()).isEqualTo("Голяма зала");
        assertThat(result.getCapacity()).isEqualTo(200);
    }

    @Test
    void updateHall_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(hallRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hallService.updateHall(id, createDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteHall_deletes_whenExists() {
        UUID id = UUID.randomUUID();
        when(hallRepository.existsById(id)).thenReturn(true);

        hallService.deleteHall(id);

        verify(hallRepository).deleteById(id);
    }

    @Test
    void deleteHall_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(hallRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> hallService.deleteHall(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(hallRepository, never()).deleteById(any());
    }

    @Test
    void getAllHalls_returnsMappedList() {
        Hall hall = Hall.builder().id(UUID.randomUUID()).name("Зала").capacity(30).build();
        when(hallRepository.findAll()).thenReturn(List.of(hall));

        List<HallResponseDTO> result = hallService.getAllHalls();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Зала");
    }
}
