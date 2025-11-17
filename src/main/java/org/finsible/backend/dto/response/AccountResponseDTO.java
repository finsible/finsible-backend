package org.finsible.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal balance;
    private Long accountGroupId;
    private String icon;
    private String currencyCode;
    private Boolean isActive;
    private Boolean isSystemDefault;

    // credit card details - include only when non-null
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal creditLimit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal availableCredit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer billingDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer dueDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean autoPayEnabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long autoPayFromAccountId;

    // linked bank account id - include only when non-null
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long linkedBankAccountId;
}
