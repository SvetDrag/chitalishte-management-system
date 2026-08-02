package bg.whiteswallow.rental.service.impl;

import bg.whiteswallow.rental.dto.EquipmentItemCreateDTO;
import bg.whiteswallow.rental.dto.EquipmentItemResponseDTO;
import bg.whiteswallow.rental.entity.EquipmentItem;
import bg.whiteswallow.rental.exception.ResourceNotFoundException;
import bg.whiteswallow.rental.repository.EquipmentItemRepository;
import bg.whiteswallow.rental.service.EquipmentItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EquipmentItemServiceImpl implements EquipmentItemService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentItemServiceImpl.class);

    private final EquipmentItemRepository equipmentItemRepository;

    public EquipmentItemServiceImpl(EquipmentItemRepository equipmentItemRepository) {
        this.equipmentItemRepository = equipmentItemRepository;
    }

    @Override
    @CacheEvict(value = "equipment", allEntries = true)
    public EquipmentItemResponseDTO createEquipmentItem(EquipmentItemCreateDTO createDTO) {
        EquipmentItem item = EquipmentItem.builder()
                .name(createDTO.getName())
                .pricePerRental(createDTO.getPricePerRental())
                .available(true)
                .build();
        item = equipmentItemRepository.save(item);
        log.info("Created equipment item '{}'", item.getName());
        return toResponseDTO(item);
    }

    @Override
    @CacheEvict(value = "equipment", allEntries = true)
    public EquipmentItemResponseDTO updateEquipmentItem(UUID id, EquipmentItemCreateDTO updateDTO) {
        EquipmentItem item = equipmentItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Техниката не е намерена."));
        item.setName(updateDTO.getName());
        item.setPricePerRental(updateDTO.getPricePerRental());
        item = equipmentItemRepository.save(item);
        log.info("Updated equipment item {}", id);
        return toResponseDTO(item);
    }

    @Override
    @CacheEvict(value = "equipment", allEntries = true)
    public void deleteEquipmentItem(UUID id) {
        if (!equipmentItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Техниката не е намерена.");
        }
        equipmentItemRepository.deleteById(id);
        log.info("Deleted equipment item {}", id);
    }

    @Override
    @Cacheable("equipment")
    public List<EquipmentItemResponseDTO> getAllEquipmentItems() {
        return equipmentItemRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private EquipmentItemResponseDTO toResponseDTO(EquipmentItem item) {
        return EquipmentItemResponseDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .pricePerRental(item.getPricePerRental())
                .available(item.isAvailable())
                .build();
    }
}
