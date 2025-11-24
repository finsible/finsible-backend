package org.finsible.backend.mapper;

import org.finsible.backend.dto.request.CategoryRequestDTO;
import org.finsible.backend.dto.response.CategoryResponseDTO;
import org.finsible.backend.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryRequestDTO categoryRequestDTO);

    @Mapping(source = "parentCategory.id", target = "parentCategoryId")
    CategoryResponseDTO toCategoryResponseDTO(Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCategoryFromDTO(CategoryRequestDTO categoryRequestDTO, @MappingTarget Category category);
}
