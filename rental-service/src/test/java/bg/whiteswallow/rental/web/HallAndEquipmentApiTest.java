package bg.whiteswallow.rental.web;

import bg.whiteswallow.rental.dto.EquipmentItemCreateDTO;
import bg.whiteswallow.rental.dto.EquipmentItemResponseDTO;
import bg.whiteswallow.rental.dto.HallCreateDTO;
import bg.whiteswallow.rental.dto.HallResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class HallAndEquipmentApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void addApiKeyHeader() {
        var interceptors = restTemplate.getRestTemplate().getInterceptors();
        if (interceptors.stream().noneMatch(ApiKeyRequestInterceptor.class::isInstance)) {
            interceptors.add(new ApiKeyRequestInterceptor());
        }
    }

    private HallCreateDTO validHallDTO() {
        HallCreateDTO dto = new HallCreateDTO();
        dto.setName("Малка зала");
        dto.setCapacity(40);
        dto.setDescription("За камерни събития");
        return dto;
    }

    private EquipmentItemCreateDTO validEquipmentDTO() {
        EquipmentItemCreateDTO dto = new EquipmentItemCreateDTO();
        dto.setName("Микрофон");
        dto.setPricePerRental(new BigDecimal("15"));
        return dto;
    }

    @Test
    void createHall_returns201() {
        ResponseEntity<HallResponseDTO> response = restTemplate.postForEntity(url("/api/halls"), validHallDTO(), HallResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("Малка зала");
    }

    @Test
    void getAllHalls_returns200() {
        restTemplate.postForEntity(url("/api/halls"), validHallDTO(), HallResponseDTO.class);

        ResponseEntity<HallResponseDTO[]> response = restTemplate.getForEntity(url("/api/halls"), HallResponseDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void updateHall_returns200AndUpdatedName() {
        HallResponseDTO created = restTemplate.postForEntity(url("/api/halls"), validHallDTO(), HallResponseDTO.class).getBody();

        HallCreateDTO update = validHallDTO();
        update.setName("Преименувана зала");
        ResponseEntity<HallResponseDTO> response = restTemplate.exchange(
                url("/api/halls/" + created.getId()),
                HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(update),
                HallResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Преименувана зала");
    }

    @Test
    void updateHall_returns404_whenMissing() {
        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/halls/" + UUID.randomUUID()),
                HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(validHallDTO()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteHall_returns204() {
        HallResponseDTO created = restTemplate.postForEntity(url("/api/halls"), validHallDTO(), HallResponseDTO.class).getBody();

        ResponseEntity<Void> response = restTemplate.exchange(
                url("/api/halls/" + created.getId()), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void createEquipmentItem_returns201AndAvailableTrue() {
        ResponseEntity<EquipmentItemResponseDTO> response =
                restTemplate.postForEntity(url("/api/equipment"), validEquipmentDTO(), EquipmentItemResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isAvailable()).isTrue();
    }

    @Test
    void getAllEquipmentItems_returns200() {
        restTemplate.postForEntity(url("/api/equipment"), validEquipmentDTO(), EquipmentItemResponseDTO.class);

        ResponseEntity<EquipmentItemResponseDTO[]> response =
                restTemplate.getForEntity(url("/api/equipment"), EquipmentItemResponseDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void updateEquipmentItem_returns200AndUpdatedPrice() {
        EquipmentItemResponseDTO created = restTemplate.postForEntity(
                url("/api/equipment"), validEquipmentDTO(), EquipmentItemResponseDTO.class).getBody();

        EquipmentItemCreateDTO update = validEquipmentDTO();
        update.setPricePerRental(new BigDecimal("25"));
        ResponseEntity<EquipmentItemResponseDTO> response = restTemplate.exchange(
                url("/api/equipment/" + created.getId()),
                HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(update),
                EquipmentItemResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getPricePerRental()).isEqualByComparingTo("25");
    }

    @Test
    void deleteEquipmentItem_returns204() {
        EquipmentItemResponseDTO created = restTemplate.postForEntity(
                url("/api/equipment"), validEquipmentDTO(), EquipmentItemResponseDTO.class).getBody();

        ResponseEntity<Void> response = restTemplate.exchange(
                url("/api/equipment/" + created.getId()), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
