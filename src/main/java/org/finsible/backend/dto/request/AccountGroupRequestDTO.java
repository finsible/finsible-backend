package org.finsible.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.finsible.backend.dto.request.groups.Create;
import org.finsible.backend.dto.request.groups.Update;
import org.finsible.backend.validator.AtLeastOneFieldNotNull;

@Data
@AtLeastOneFieldNotNull(groups = Update.class)
public class AccountGroupRequestDTO {
    @NotBlank(groups = Create.class, message = "Account group name must not be blank")
    @Size(min = 1, max = 255, groups = {Create.class, Update.class}, message = "Account group name must be between 1 to 255 characters")
    private String name;

    @Size(max = 255, groups = {Create.class, Update.class}, message = "Description must not exceed 255 characters")
    private String description;

    @Size(max = 255, groups = {Create.class, Update.class}, message = "Icon must not exceed 255 characters")
    private String icon;

    private Integer displayOrder;
}
