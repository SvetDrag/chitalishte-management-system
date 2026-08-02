package bg.whiteswallow.manager.service;

import bg.whiteswallow.manager.exception.ResourceNotFoundException;
import bg.whiteswallow.manager.model.dto.inventory.InventoryItemAddDTO;
import bg.whiteswallow.manager.model.entity.inventory.InventoryItem;
import bg.whiteswallow.manager.model.entity.inventory.ItemStatus;
import bg.whiteswallow.manager.model.entity.user.User;
import bg.whiteswallow.manager.repository.InventoryItemRepository;
import bg.whiteswallow.manager.repository.UserRepository;
import bg.whiteswallow.manager.service.impl.InventoryItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class InventoryItemServiceImplTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InventoryItemServiceImpl inventoryItemService;

    private InventoryItemAddDTO addDTO;
    private User user;

    @BeforeEach
    void setUp() {
        addDTO = new InventoryItemAddDTO();
        addDTO.setName("Костюм");
        addDTO.setItemCondition("Ново");
        addDTO.setStatus(ItemStatus.AVAILABLE);
        user = User.builder().id(UUID.randomUUID()).username("member").build();
    }

    @Test
    void addItem_savesItem() {
        inventoryItemService.addItem(addDTO);

        verify(inventoryItemRepository).save(any(InventoryItem.class));
    }

    @Test
    void getAllItems_returnsRepositoryResult() {
        List<InventoryItem> items = List.of(InventoryItem.builder().name("A").build());
        when(inventoryItemRepository.findAll()).thenReturn(items);

        assertThat(inventoryItemService.getAllItems()).isEqualTo(items);
    }

    @Test
    void deleteItem_delegatesToRepository() {
        UUID id = UUID.randomUUID();

        inventoryItemService.deleteItem(id);

        verify(inventoryItemRepository).deleteById(id);
    }

    @Test
    void getItemForEdit_mapsEntityToDto_whenFound() {
        UUID id = UUID.randomUUID();
        InventoryItem item = InventoryItem.builder().id(id).name("Костюм").itemCondition("Ново").status(ItemStatus.AVAILABLE).build();
        when(inventoryItemRepository.findById(id)).thenReturn(Optional.of(item));

        InventoryItemAddDTO result = inventoryItemService.getItemForEdit(id);

        assertThat(result.getName()).isEqualTo("Костюм");
    }

    @Test
    void getItemForEdit_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(inventoryItemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryItemService.getItemForEdit(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateItem_updatesFields_whenFound() {
        UUID id = UUID.randomUUID();
        InventoryItem item = InventoryItem.builder().id(id).name("Old").itemCondition("Old").status(ItemStatus.AVAILABLE).build();
        when(inventoryItemRepository.findById(id)).thenReturn(Optional.of(item));

        inventoryItemService.updateItem(id, addDTO);

        assertThat(item.getName()).isEqualTo("Костюм");
        verify(inventoryItemRepository).save(item);
    }

    @Test
    void updateItem_throwsResourceNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(inventoryItemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryItemService.updateItem(id, addDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void lendItem_setsStatusBorrowedAndBorrower() {
        UUID itemId = UUID.randomUUID();
        InventoryItem item = InventoryItem.builder().id(itemId).name("Костюм").status(ItemStatus.AVAILABLE).build();
        when(inventoryItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        inventoryItemService.lendItem(itemId, user.getId());

        assertThat(item.getStatus()).isEqualTo(ItemStatus.BORROWED);
        assertThat(item.getBorrowedBy()).isEqualTo(user);
        assertThat(item.getBorrowedOn()).isNotNull();
    }

    @Test
    void lendItem_throwsResourceNotFound_whenUserMissing() {
        UUID itemId = UUID.randomUUID();
        InventoryItem item = InventoryItem.builder().id(itemId).name("Костюм").status(ItemStatus.AVAILABLE).build();
        when(inventoryItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryItemService.lendItem(itemId, user.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void returnItem_clearsBorrowerAndSetsAvailable() {
        UUID itemId = UUID.randomUUID();
        InventoryItem item = InventoryItem.builder().id(itemId).name("Костюм").status(ItemStatus.BORROWED)
                .borrowedBy(user).borrowedOn(LocalDateTime.now()).build();
        when(inventoryItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        inventoryItemService.returnItem(itemId);

        assertThat(item.getStatus()).isEqualTo(ItemStatus.AVAILABLE);
        assertThat(item.getBorrowedBy()).isNull();
        assertThat(item.getBorrowedOn()).isNull();
    }

    @Test
    void getOverdueItems_delegatesToRepositoryWithThreshold() {
        List<InventoryItem> overdue = List.of(InventoryItem.builder().name("Overdue").status(ItemStatus.BORROWED).build());
        when(inventoryItemRepository.findAllByStatusAndBorrowedOnBefore(org.mockito.ArgumentMatchers.eq(ItemStatus.BORROWED), any()))
                .thenReturn(overdue);

        List<InventoryItem> result = inventoryItemService.getOverdueItems(14);

        assertThat(result).isEqualTo(overdue);
    }
}
