package org.finsible.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDTO {
    private Long id;
    private String type;
    private String totalAmount;
    private String transactionDate;
    private Long categoryId;
    private String categoryName;
    private String description;
    private String currency;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long toAccountId;
//    private String toAccountName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long fromAccountId;
//    private String fromAccountName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long spaceId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userShare;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean isSplit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long paidByUserId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String paidByUserName;
}
