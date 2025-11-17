package org.finsible.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountGroupResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String icon;
//    private Integer displayOrder;  enable later if needed
    private Boolean isSystemDefault;
}
