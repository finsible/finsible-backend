package org.finsible.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyResponseDTO {
    private Long id;
    private String name;
    private String code;
    private String symbol;
}
