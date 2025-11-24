package org.finsible.backend.service;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.CustomExceptionHandler.EntityNotFoundException;
import org.finsible.backend.CustomExceptionHandler.UserNotFoundException;
import org.finsible.backend.dto.request.CategoryRequestDTO;
import org.finsible.backend.dto.response.CategoryResponseDTO;
import org.finsible.backend.mapper.CategoryMapper;
import org.finsible.backend.repository.CategoryRepository;
import org.finsible.backend.repository.UserRepository;
import org.finsible.backend.entity.Category;
import org.finsible.backend.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoryService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    public CategoryService(UserRepository userRepository, CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // if user has not edited categories, then we return default categories
        List<Category> categories = getDefaultCategories();

        if(!user.isCategoriesEdited()) {
            logger.info("Getting default categories for user with id: {}", userId);
            return categories.stream().map(categoryMapper::toCategoryResponseDTO).toList();
        }

        logger.info("Getting default and user-specific categories for user with id: {}", userId);
        categories.addAll(categoryRepository.findCategoriesByCreatedBy_Id(userId));
        return categories.stream().map(categoryMapper::toCategoryResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getCategoriesByType(String userId, Category.CategoryType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        List<Category> categoriesByType = categoryRepository.findCategoriesByTypeAndCreatedBy(type, null);

        if(!user.isCategoriesEdited()) {
            logger.info("Getting default categories of type {} for user with id: {}", type, userId);
            return categoriesByType.stream().map(categoryMapper::toCategoryResponseDTO).toList();
        }

        logger.info("Getting default and user-specific categories of type {} for user with id: {}", type, userId);
        categoriesByType.addAll(categoryRepository.findCategoriesByTypeAndCreatedBy(type, user));
        return categoriesByType.stream().map(categoryMapper::toCategoryResponseDTO).toList();
    }

    public List<Category> getDefaultCategories() {
        return categoryRepository.findCategoriesByCreatedBy_Id(null);
    }

    @Transactional
    public CategoryResponseDTO createDefaultCategory(CategoryRequestDTO category) throws BadRequestException {
        Category defaultCategory = categoryMapper.toCategory(category);
        defaultCategory.setCreatedBy(null);
        validateParentCategory(null, category, defaultCategory);
        categoryRepository.save(defaultCategory);
        logger.info("Created new default category with id: {}", defaultCategory.getId());
        return categoryMapper.toCategoryResponseDTO(defaultCategory);
    }

    @Transactional
    public CategoryResponseDTO updateDefaultCategory(Long categoryId, CategoryRequestDTO categoryRequestDTO) throws BadRequestException {
        Category existingDefaultCategory = categoryRepository.findByIdAndCreatedBy_Id(categoryId, null)
                .orElseThrow(() -> new EntityNotFoundException("Category does not exist with categoryId: " + categoryId));

        categoryMapper.updateCategoryFromDTO(categoryRequestDTO, existingDefaultCategory);
        validateParentCategory(null, categoryRequestDTO, existingDefaultCategory);
        categoryRepository.save(existingDefaultCategory);
        logger.info("Updated default category with categoryId: {}", categoryId);
        return categoryMapper.toCategoryResponseDTO(existingDefaultCategory);
    }

    @Transactional
    public void deleteDefaultCategory(Long categoryId) {
        Category defaultCategory = categoryRepository.findByIdAndCreatedBy_Id(categoryId, null)
                .orElseThrow(() -> new EntityNotFoundException("Category does not exist with id: " + categoryId));

        categoryRepository.deleteById(categoryId);
        logger.info("Deleted default category with id: {}", categoryId);
    }

    @Transactional
    public CategoryResponseDTO createUserCategory(String userId, CategoryRequestDTO categoryRequestDTO) throws BadRequestException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Category category = categoryMapper.toCategory(categoryRequestDTO);

        if(!user.isCategoriesEdited()){
            user.setCategoriesEdited(true);
            userRepository.save(user);
        }

        validateParentCategory(userId, categoryRequestDTO, category);

        category.setCreatedBy(user);
        categoryRepository.save(category);
        logger.info("Created new category with id: {} for user: {}", category.getId(), userId);
        return categoryMapper.toCategoryResponseDTO(category);
    }

    // todo: refactor this method and clear the validations
    private void validateParentCategory(String userId, CategoryRequestDTO categoryRequestDTO, Category category) throws BadRequestException {
        Boolean isSubCategory = categoryRequestDTO.getIsSubCategory();
        if(isSubCategory == null){
            // not removing parent category info if isSubCategory is not provided
            return;
        }
        if (!isSubCategory) {
            category.setParentCategory(null);
            return;
        }

        Long parentId = categoryRequestDTO.getParentCategoryId();
        if (parentId == null) {
            throw new BadRequestException("Parent category id must be provided for sub-categories");
        }

        Category parentCategory = categoryRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent category does not exist with id: " + parentId));

        // todo: Add a check to ensure default categories can only have default parents but user categories can have both default and user parents.
        if (parentCategory.getCreatedBy() != null && !parentCategory.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("Parent category does not belong to the user");
        }

        category.setParentCategory(parentCategory);
    }

    @Transactional
    public CategoryResponseDTO updateUserCategory(String userId, Long categoryId, CategoryRequestDTO categoryRequestDTO) throws BadRequestException {
        Category existingCategory = categoryRepository.findByIdAndCreatedBy_Id(categoryId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Category does not exist with categoryId: " + categoryId));

        categoryMapper.updateCategoryFromDTO(categoryRequestDTO, existingCategory);
        validateParentCategory(userId, categoryRequestDTO, existingCategory);

        categoryRepository.save(existingCategory);
        logger.info("Updated category with categoryId: {}", categoryId);
        return categoryMapper.toCategoryResponseDTO(existingCategory);
    }

    // do we allow user to delete default category? - no
    @Transactional
    public void deleteUserCategory(String userId, Long categoryId) {
        Category category = categoryRepository.findByIdAndCreatedBy_Id(categoryId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Category does not exist with id: " + categoryId));

        // todo: check if category is used in any transaction or other checks, if yes, prevent deletion or show warning
        categoryRepository.deleteById(categoryId);
        logger.info("Deleted category with id: {}", categoryId);
    }
}