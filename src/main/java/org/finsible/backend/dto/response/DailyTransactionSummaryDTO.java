package org.finsible.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyTransactionSummaryDTO {
    private Long date;
    private BigDecimal incomeSum;
    private BigDecimal expenseSum;
}
