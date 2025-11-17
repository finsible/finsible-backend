package org.finsible.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageResponseDTO {
    private Long id;
    private String name;
    private String code;
}
