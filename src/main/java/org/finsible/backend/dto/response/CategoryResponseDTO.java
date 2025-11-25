package org.finsible.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.finsible.backend.entity.Category;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String icon;
    private Category.CategoryType type; // "INCOME" or "EXPENSE"
    private boolean isSubCategory;
    private Long parentCategoryId;
}
