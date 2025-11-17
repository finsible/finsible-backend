package org.finsible.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.finsible.backend.dto.request.groups.Create;
import org.finsible.backend.dto.request.groups.Update;
import org.finsible.backend.validator.AtLeastOneFieldNotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@AtLeastOneFieldNotNull(groups = Update.class)
public class DebitCardAccountRequestDTO {
    @NotBlank(groups = Create.class, message = "Account Name is required")
    @Size(groups = {Create.class, Update.class}, max = 255, message = "Name can have maximum 255 characters")
    private String name;

    @Size(groups = {Create.class, Update.class}, max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(groups = {Create.class, Update.class}, max = 255, message = "Icon must not exceed 255 characters")
    private String icon;

    @Size(groups = {Create.class, Update.class}, min = 3, max = 3, message = "Currency code must be a 3-letter ISO code")
    private String currencyCode;

    private Boolean isActive;

    @NotNull(groups = Create.class, message = "Linked Bank Account is required for Debit Card Account")
    private Long linkedBankAccountId;
}
