package org.finsible.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageRequestDTO {
    @NotBlank(message = "Language code is required")
    private String code;

    @NotBlank(message = "Language name is required")
    private String name;
}
