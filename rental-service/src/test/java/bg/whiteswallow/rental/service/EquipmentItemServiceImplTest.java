package bg.whiteswallow.rental.service;

import bg.whiteswallow.rental.dto.EquipmentItemCreateDTO;
import bg.whiteswallow.rental.dto.EquipmentItemResponseDTO;
import bg.whiteswallow.rental.entity.EquipmentItem;
import bg.whiteswallow.rental.exception.ResourceNotFoundException;
import bg.whiteswallow.rental.repository.EquipmentItemRepository;
import bg.whiteswallow.rental.service.impl.EquipmentItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class EquipmentItemServiceImplTest {

    @Mock
    private EquipmentItemRepository equipmentItemRepository;

    @InjectMocks
    private EquipmentItemServiceImpl equipmentItemService;

    private EquipmentItemCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        createDTO = new EquipmentItemCreateDTO();
        createDTO.setName("Прожектор");
        createDTO.setPricePerRental(new BigDecimal("50"));
    }

    @Test
    void createEquipmentItem_savesAsAvailableAndReturnsDto() {
        when(equipmentItemRepository.save(any(EquipmentItem.class))).thenAnswer(invocation -> {
            EquipmentItem item = invocation.getArgument(0);
            item.setId(UUID.randomUUID());
            return item;
        });

        EquipmentItemResponseDTO result = equipmentItemService.createEquipmentItem(createDTO);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Прожектор");
        assertThat(result.isAvailable()).isTrue();
    }

    @Test
    void updateEquipmentItem_updatesAndReturnsDto_whenFound() {
        UUID id = UUID.randomUUID();
        EquipmentItem existing = EquipmentItem.builder().id(id).name("Стар микрофон").pricePerRental(new BigDecimal("10")).available(true).build();
        when(equipmentItemRepository.findById(id)).thenReturn(Optional.of(existing));
        when(equipmentItemRepository.save(any(EquipmentItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EquipmentItemResponseDTO result = equipmentItemService.updateEquipmentItem(id, createDTO);

        assertThat(result.getName()).isEqualTo("Прожектор");
        assertThat(result.getPricePerRental()).isEqualByComparingTo("50");
    }

    @Test
    void updateEquipmentItem_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(equipmentItemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentItemService.updateEquipmentItem(id, createDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteEquipmentItem_deletes_whenExists() {
        UUID id = UUID.randomUUID();
        when(equipmentItemRepository.existsById(id)).thenReturn(true);

        equipmentItemService.deleteEquipmentItem(id);

        verify(equipmentItemRepository).deleteById(id);
    }

    @Test
    void deleteEquipmentItem_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(equipmentItemRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> equipmentItemService.deleteEquipmentItem(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(equipmentItemRepository, never()).deleteById(any());
    }

    @Test
    void getAllEquipmentItems_returnsMappedList() {
        EquipmentItem item = EquipmentItem.builder().id(UUID.randomUUID()).name("Тонколони").pricePerRental(new BigDecimal("20")).available(true).build();
        when(equipmentItemRepository.findAll()).thenReturn(List.of(item));

        List<EquipmentItemResponseDTO> result = equipmentItemService.getAllEquipmentItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Тонколони");
    }
}
