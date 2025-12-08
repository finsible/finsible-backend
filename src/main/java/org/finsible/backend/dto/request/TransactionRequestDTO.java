package org.finsible.backend.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.finsible.backend.dto.request.groups.Create;
import org.finsible.backend.dto.request.groups.Update;
import org.finsible.backend.entity.Type;
import org.finsible.backend.validator.AtLeastOneFieldNotNull;
import java.math.BigDecimal;

@Data
@AtLeastOneFieldNotNull(groups = Update.class)
public class TransactionRequestDTO {
    @NotNull(message = "Transaction type must be provided", groups = {Create.class})
    private Type type; // "INCOME", "EXPENSE", "TRANSFER"

    @NotNull(message = "Total amount must be provided", groups = {Create.class})
    @Digits(integer = 15, fraction = 4, message = "Total amount must have at most 15 integral digits and 4 fractional digits.", groups = {Create.class, Update.class})
    private BigDecimal totalAmount;

    @NotNull(message = "Transaction date must be provided", groups = {Create.class})
    private Long transactionDate; // in milliseconds

    @NotNull(message = "Category ID must be provided", groups = {Create.class})
    private Long categoryId;

    private Long toAccountId; // used in income and transfer

    private Long fromAccountId; // used in expense and transfer

    @Size(max = 255, message = "Description can have maximum 255 characters", groups = {Create.class, Update.class})
    private String description;

    private Long spaceId;

    private BigDecimal userShare;

    private Boolean isSplit;

    private Long paidByUserId; // User who paid the amount

    @Size(max = 3, min=3, message = "Currency code can have maximum 3 characters", groups = {Create.class, Update.class})
    private String currency;
}
