package org.finsible.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.finsible.backend.entity.Type;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String icon;
    private Type type;
    private Boolean isSubCategory;
    private Long parentCategoryId;
}
