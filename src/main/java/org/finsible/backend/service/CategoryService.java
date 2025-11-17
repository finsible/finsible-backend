package org.finsible.backend.service;

import jakarta.transaction.Transactional;
import org.finsible.backend.AppConstants;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.ErrorDetails;
import org.finsible.backend.repository.CategoryRepository;
import org.finsible.backend.repository.UserRepository;
import org.finsible.backend.entity.Category;
import org.finsible.backend.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ResponseEntity<BaseResponse<List<Category>>> getCategories(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            BaseResponse<List<Category>> response = new BaseResponse<>("User not found", false, null);
            return ResponseEntity.status(404).body(response);
        }
        // if user has not edited categories, then we return default categories
        List<Category> categories = categoryRepository.findCategoriesByCreatedBy_Id(null);
        BaseResponse<List<Category>> response = new BaseResponse<>("User categories returned", true);

        if (!user.isCategoriesEdited()) {
            response.setData(categories);
            return ResponseEntity.ok(response);
        }
        categories.addAll(categoryRepository.findCategoriesByCreatedBy_Id(userId));
        response.setData(categories);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<BaseResponse<List<Category>>> getCategoriesByType(String userId, Category.CategoryType type) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            BaseResponse<List<Category>> response = new BaseResponse<>("User not found", false, null);
            return ResponseEntity.status(404).body(response);
        }
        BaseResponse<List<Category>> response = new BaseResponse<>("Categories of type returned", true);
        List<Category> categoriesByType = categoryRepository.findCategoriesByTypeAndCreatedBy(type, null);

        if(!user.isCategoriesEdited()) {
            response.setData(categoriesByType);
            return ResponseEntity.ok(response);
        }
        categoriesByType.addAll(categoryRepository.findCategoriesByTypeAndCreatedBy(type, user));
        response.setData(categoriesByType);
        return ResponseEntity.ok(response);
    }

    public BaseResponse<List<Category>> getDefaultCategories() {
        List<Category> categories = categoryRepository.findCategoriesByCreatedBy_Id(null);
        return new BaseResponse<>("Default categories returned", true, categories);
    }

    // not for user
    public ResponseEntity<BaseResponse<Category>> createDefaultCategory(Category category) {
        category.setCreatedBy(null);
        categoryRepository.save(category);
        BaseResponse<Category> response = new BaseResponse<>("Default category created successfully", true, category);
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<BaseResponse<Category>> updateUserCategory(String userId, Category category) {
        BaseResponse<Category> response;
        try {
            //todo verify ownership of the category with the authenticated user before update
            Category existingCategory = categoryRepository.findById(category.getId()).orElse(null);
            if (existingCategory == null || existingCategory.getCreatedBy() == null) {
                response = new BaseResponse<>("Category not found or is a default category", false, null);
                return ResponseEntity.status(404).body(response);
            }
            else {
                if(category.getName()!=null) existingCategory.setName(category.getName());
                if(category.getIcon()!=null) existingCategory.setIcon(category.getIcon());
                if (category.getType()!=null) existingCategory.setType(category.getType());
                categoryRepository.save(existingCategory);
                response = new BaseResponse<>("Category updated successfully", true, existingCategory);
            }
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            //some error occurred then need to do any rollback?
            logger.error("Error while creating user category", e);
            return errorHandler(e, new BaseResponse<>("Error while updating category", false, null));
        }
    }

    @Transactional
    public ResponseEntity<BaseResponse<Category>> createUserCategory(String userId, Category category) {
        BaseResponse<Category> response;
        try {
            User user = userRepository.findById(userId).orElse(null);
            category.setCreatedBy(user);
            categoryRepository.save(category); // check changes in category after save - do we see id? yes
            if(user!=null && !user.isCategoriesEdited()){
                user.setCategoriesEdited(true);
                userRepository.save(user);
            }
            response = new BaseResponse<>("Category created successfully", true, category);
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            //some error occurred then need to do any rollback?
            logger.error("Error while creating category", e);
            return errorHandler(e, new BaseResponse<>("Error while creating category", false, null));
        }
    }

    // do we allow user to delete default category? - no
    @Transactional
    public ResponseEntity<BaseResponse<Category>> deleteUserCategory(String userId, Long categoryId) {
        BaseResponse<Category> response;
        try {
            //todo verify ownership of the category with the authenticated user before deletion
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                response = new BaseResponse<>("Category not found", false, null);
                return ResponseEntity.status(404).body(response);
            }
            if(category.getCreatedBy()==null) {
                response = new BaseResponse<>("Cannot delete default category", false, null);
                return ResponseEntity.status(400).body(response);
            }
            categoryRepository.deleteById(categoryId);
            response = new BaseResponse<>("Category deleted successfully", true, null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error while deleting category", e);
            return errorHandler(e, new BaseResponse<>("Error while deleting category", false, null));
        }
    }

    public ResponseEntity<BaseResponse<Category>> deleteDefaultCategory(Long categoryId) {
        BaseResponse<Category> response;
        try {
            categoryRepository.deleteById(categoryId);
            response = new BaseResponse<>("Default Category deleted successfully", true, null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error while deleting category", e);
            return errorHandler(e, new BaseResponse<>("Error while deleting category", false, null));
        }
    }

    private ResponseEntity<BaseResponse<Category>> errorHandler(Exception e, BaseResponse<Category> response) {
        ErrorDetails errorDetails=new ErrorDetails(AppConstants.INTERNAL_SERVER_ERROR, e.getMessage(), e.getCause() != null ? e.getCause().toString() : "");
        logger.error("Error details: {}", errorDetails);
        return ResponseEntity.internalServerError().body(response);
    }
}
