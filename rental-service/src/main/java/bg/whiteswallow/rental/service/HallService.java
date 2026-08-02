package bg.whiteswallow.rental.service;

import bg.whiteswallow.rental.dto.HallCreateDTO;
import bg.whiteswallow.rental.dto.HallResponseDTO;

import java.util.List;
import java.util.UUID;

public interface HallService {
    HallResponseDTO createHall(HallCreateDTO createDTO);
    HallResponseDTO updateHall(UUID id, HallCreateDTO updateDTO);
    void deleteHall(UUID id);
    List<HallResponseDTO> getAllHalls();
}
