package bg.whiteswallow.rental.service.impl;

import bg.whiteswallow.rental.dto.HallCreateDTO;
import bg.whiteswallow.rental.dto.HallResponseDTO;
import bg.whiteswallow.rental.entity.Hall;
import bg.whiteswallow.rental.exception.ResourceNotFoundException;
import bg.whiteswallow.rental.repository.HallRepository;
import bg.whiteswallow.rental.service.HallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class HallServiceImpl implements HallService {

    private static final Logger log = LoggerFactory.getLogger(HallServiceImpl.class);

    private final HallRepository hallRepository;

    public HallServiceImpl(HallRepository hallRepository) {
        this.hallRepository = hallRepository;
    }

    @Override
    @CacheEvict(value = "halls", allEntries = true)
    public HallResponseDTO createHall(HallCreateDTO createDTO) {
        Hall hall = Hall.builder()
                .name(createDTO.getName())
                .capacity(createDTO.getCapacity())
                .description(createDTO.getDescription())
                .build();
        hall = hallRepository.save(hall);
        log.info("Created hall '{}'", hall.getName());
        return toResponseDTO(hall);
    }

    @Override
    @CacheEvict(value = "halls", allEntries = true)
    public HallResponseDTO updateHall(UUID id, HallCreateDTO updateDTO) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Залата не е намерена."));
        hall.setName(updateDTO.getName());
        hall.setCapacity(updateDTO.getCapacity());
        hall.setDescription(updateDTO.getDescription());
        hall = hallRepository.save(hall);
        log.info("Updated hall {}", id);
        return toResponseDTO(hall);
    }

    @Override
    @CacheEvict(value = "halls", allEntries = true)
    public void deleteHall(UUID id) {
        if (!hallRepository.existsById(id)) {
            throw new ResourceNotFoundException("Залата не е намерена.");
        }
        hallRepository.deleteById(id);
        log.info("Deleted hall {}", id);
    }

    @Override
    @Cacheable("halls")
    public List<HallResponseDTO> getAllHalls() {
        return hallRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private HallResponseDTO toResponseDTO(Hall hall) {
        return HallResponseDTO.builder()
                .id(hall.getId())
                .name(hall.getName())
                .capacity(hall.getCapacity())
                .description(hall.getDescription())
                .build();
    }
}
