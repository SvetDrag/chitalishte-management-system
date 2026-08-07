package bg.whiteswallow.rental.web;

import bg.whiteswallow.rental.dto.HallCreateDTO;
import bg.whiteswallow.rental.dto.HallResponseDTO;
import bg.whiteswallow.rental.dto.RentalRequestCreateDTO;
import bg.whiteswallow.rental.dto.RentalRequestResponseDTO;
import bg.whiteswallow.rental.dto.RentalStatusUpdateDTO;
import bg.whiteswallow.rental.entity.RentalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class RentalRequestControllerApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private UUID hallId;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static RentalStatusUpdateDTO statusUpdate(RentalStatus status) {
        RentalStatusUpdateDTO dto = new RentalStatusUpdateDTO();
        dto.setStatus(status);
        return dto;
    }

    private RentalRequestCreateDTO validCreateDTO() {
        RentalRequestCreateDTO dto = new RentalRequestCreateDTO();
        dto.setHallId(hallId);
        dto.setRenterName("Maria Petrova");
        dto.setRenterPhone("0888999888");
        dto.setRenterEmail("maria@example.com");
        dto.setStartDateTime(LocalDateTime.now().plusDays(2));
        dto.setEndDateTime(LocalDateTime.now().plusDays(2).plusHours(3));
        dto.setPurpose("Birthday party");
        dto.setPrice(new BigDecimal("300"));
        return dto;
    }

    @BeforeEach
    void setUp() {
        var interceptors = restTemplate.getRestTemplate().getInterceptors();
        if (interceptors.stream().noneMatch(ApiKeyRequestInterceptor.class::isInstance)) {
            interceptors.add(new ApiKeyRequestInterceptor());
        }

        HallCreateDTO hallDTO = new HallCreateDTO();
        hallDTO.setName("Голяма зала");
        hallDTO.setCapacity(150);
        HallResponseDTO hall = restTemplate.postForEntity(url("/api/halls"), hallDTO, HallResponseDTO.class).getBody();
        hallId = hall.getId();
    }

    @Test
    void createRentalRequest_returns201AndPersistedResource() {
        ResponseEntity<RentalRequestResponseDTO> response =
                restTemplate.postForEntity(url("/api/rentals"), validCreateDTO(), RentalRequestResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(RentalStatus.PENDING);
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getHall().getId()).isEqualTo(hallId);
    }

    @Test
    void createRentalRequest_returns400_whenPayloadInvalid() {
        RentalRequestCreateDTO invalid = validCreateDTO();
        invalid.setRenterEmail("not-an-email");

        ResponseEntity<String> response = restTemplate.postForEntity(url("/api/rentals"), invalid, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createRentalRequest_returns404_whenHallDoesNotExist() {
        RentalRequestCreateDTO invalid = validCreateDTO();
        invalid.setHallId(UUID.randomUUID());

        ResponseEntity<String> response = restTemplate.postForEntity(url("/api/rentals"), invalid, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllRentalRequests_returns200() {
        restTemplate.postForEntity(url("/api/rentals"), validCreateDTO(), RentalRequestResponseDTO.class);

        ResponseEntity<RentalRequestResponseDTO[]> response =
                restTemplate.getForEntity(url("/api/rentals"), RentalRequestResponseDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void updateStatus_returns200AndUpdatedStatus() {
        RentalRequestResponseDTO created = restTemplate.postForEntity(
                url("/api/rentals"), validCreateDTO(), RentalRequestResponseDTO.class).getBody();

        restTemplate.put(url("/api/rentals/" + created.getId() + "/status"),
                statusUpdate(RentalStatus.CONFIRMED));

        ResponseEntity<RentalRequestResponseDTO[]> listResponse =
                restTemplate.getForEntity(url("/api/rentals"), RentalRequestResponseDTO[].class);
        assertThat(listResponse.getBody())
                .filteredOn(dto -> dto.getId().equals(created.getId()))
                .extracting(RentalRequestResponseDTO::getStatus)
                .containsExactly(RentalStatus.CONFIRMED);
    }

    @Test
    void updateStatus_returns404_whenRentalDoesNotExist() {
        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/rentals/" + UUID.randomUUID() + "/status"),
                HttpMethod.PUT,
                new HttpEntity<>(statusUpdate(RentalStatus.CONFIRMED)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteRentalRequest_returns204() {
        RentalRequestResponseDTO created = restTemplate.postForEntity(
                url("/api/rentals"), validCreateDTO(), RentalRequestResponseDTO.class).getBody();

        ResponseEntity<Void> response = restTemplate.exchange(
                url("/api/rentals/" + created.getId()),
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
