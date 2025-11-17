package org.finsible.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.finsible.backend.dto.request.groups.Create;
import org.finsible.backend.dto.request.groups.Update;
import org.finsible.backend.validator.AtLeastOneFieldNotNull;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@AtLeastOneFieldNotNull(groups = Update.class)
public class CreditCardAccountRequestDTO {
    @NotBlank(message = "Account Name is required", groups = Create.class)
    @Size(max = 255, message = "Name can have maximum 255 characters", groups = {Create.class, Update.class})
    private String name;

    @Size(groups = {Create.class, Update.class}, max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(groups = {Create.class, Update.class}, max = 255, message = "Icon must not exceed 255 characters")
    private String icon;

    @Size(groups = {Create.class, Update.class}, min = 3, max = 3, message = "Currency code must be a 3-letter ISO code")
    private String currencyCode;

    @NotNull(groups = Create.class, message = "Credit limit is required for Credit Card Account")
    @Positive(groups = {Create.class, Update.class}, message = "Credit limit must be a positive value")
    private BigDecimal creditLimit;

    // if not provided, will be set to credit limit when created
    // it should be a number and should not be greater than credit limit , can be negative
    @NumberFormat // todo review annotation
    private BigDecimal availableCredit; // will be equal to the account balance when created

    @Min(value = 1, groups = {Create.class, Update.class}, message = "Billing date must be between 1 and 31")
    @Max(value = 31, groups = {Create.class, Update.class}, message = "Billing date must be between 1 and 31")
    private Integer billingDate;

    @Min(value = 1, groups = {Create.class, Update.class}, message = "Billing date must be between 1 and 31")
    @Max(value = 31, groups = {Create.class, Update.class}, message = "Billing date must be between 1 and 31")
    private Integer dueDate;

    private Boolean isActive;  // db default is true

    private Boolean autoPayEnabled;  // db default is false
    private Long autoPayFromAccountId;

//    @AssertTrue(message = "Auto pay from account is required when aut pay is enabled", groups = {Create.class, Update.class})
//    private boolean isAutoPayLinkValid() {
//        return !autoPayEnabled || autoPayFromAccountId != null;
//    }
}
