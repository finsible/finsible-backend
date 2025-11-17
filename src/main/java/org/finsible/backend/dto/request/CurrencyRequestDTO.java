package org.finsible.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRequestDTO {
    @NotBlank(message = "currency name is mandatory")
    private String name;

    @NotBlank(message = "currency code is mandatory")
    private String code;

    @NotBlank(message = "currency symbol is mandatory")
    private String symbol;
}
