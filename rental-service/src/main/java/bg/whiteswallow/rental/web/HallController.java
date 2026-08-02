package bg.whiteswallow.rental.web;

import bg.whiteswallow.rental.dto.HallCreateDTO;
import bg.whiteswallow.rental.dto.HallResponseDTO;
import bg.whiteswallow.rental.service.HallService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/halls")
public class HallController {

    private final HallService hallService;

    public HallController(HallService hallService) {
        this.hallService = hallService;
    }

    @GetMapping
    public ResponseEntity<List<HallResponseDTO>> getAllHalls() {
        return ResponseEntity.ok(hallService.getAllHalls());
    }

    @PostMapping
    public ResponseEntity<HallResponseDTO> createHall(@Valid @RequestBody HallCreateDTO createDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hallService.createHall(createDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HallResponseDTO> updateHall(@PathVariable UUID id, @Valid @RequestBody HallCreateDTO updateDTO) {
        return ResponseEntity.ok(hallService.updateHall(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHall(@PathVariable UUID id) {
        hallService.deleteHall(id);
        return ResponseEntity.noContent().build();
    }
}
