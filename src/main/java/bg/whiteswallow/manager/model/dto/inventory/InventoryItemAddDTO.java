package bg.whiteswallow.manager.model.dto.inventory;

import bg.whiteswallow.manager.model.entity.inventory.ItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryItemAddDTO {

    @NotBlank(message = "Името на предмета е задължително!")
    private String name;

    @NotBlank(message = "Посочете състояние (напр. 'Ново', 'Скъсано', 'За ремонт')")
    private String itemCondition;

    @NotNull(message = "Моля, изберете статус на наличност!")
    private ItemStatus status;
}