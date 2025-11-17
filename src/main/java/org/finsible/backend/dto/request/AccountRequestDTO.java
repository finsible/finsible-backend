package org.finsible.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.finsible.backend.dto.request.groups.Create;
import org.finsible.backend.dto.request.groups.Update;
import org.finsible.backend.validator.AtLeastOneFieldNotNull;

@Data
@AtLeastOneFieldNotNull(groups = Update.class)
public class AccountRequestDTO {
    @NotBlank(groups = Create.class, message = "Account name must not be blank")
    @Size(groups = {Create.class, Update.class}, min = 1, max = 255, message = "Account name must be between 1 to 255 characters")
    private String name;

    @Size(groups = {Create.class, Update.class}, max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(groups = {Create.class, Update.class}, max = 255, message = "Icon must not exceed 255 characters")
    private String icon;

    @Size(groups = {Create.class, Update.class}, min = 3, max = 3, message = "Currency code must be a 3-letter ISO code")
    private String currencyCode;

    private boolean isActive;
    private boolean isSystemDefault;
}
