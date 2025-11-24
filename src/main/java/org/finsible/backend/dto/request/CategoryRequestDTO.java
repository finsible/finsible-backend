package org.finsible.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.finsible.backend.dto.request.groups.Create;
import org.finsible.backend.dto.request.groups.Update;
import org.finsible.backend.entity.Category;
import org.finsible.backend.validator.AtLeastOneFieldNotNull;

@Data
@AtLeastOneFieldNotNull(groups = Update.class)
public class CategoryRequestDTO {

    @NotBlank(groups = Create.class, message = "Category name must be provided")
    @Size(groups = {Create.class, Update.class}, max=255, message = "Category name must not exceed 255 characters")
    private String name;

    @Size(groups = {Create.class, Update.class}, max=255, message = "Icon must not exceed 255 characters")
    private String icon;

    // valid values: "INCOME", "EXPENSE"
    @NotNull(groups = Create.class, message = "Category type must be provided")
    private Category.CategoryType type;

    private Boolean isSubCategory;
    private Long parentCategoryId;
}
