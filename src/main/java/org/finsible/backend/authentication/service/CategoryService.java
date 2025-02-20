package org.finsible.backend.authentication.service;

import jakarta.transaction.Transactional;
import org.finsible.backend.AppConstants;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.ErrorDetails;
import org.finsible.backend.authentication.entity.Category;
import org.finsible.backend.authentication.entity.User;
import org.finsible.backend.authentication.entity.UserCategory;
import org.finsible.backend.authentication.entity.UserCategoryId;
import org.finsible.backend.authentication.repository.CategoryRepository;
import org.finsible.backend.authentication.repository.UserCategoryRepository;
import org.finsible.backend.authentication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class CategoryService {

    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private UserCategoryRepository userCategoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    @Autowired
    public void setUserCategoryRepository(UserCategoryRepository userCategoryRepository) {
        this.userCategoryRepository = userCategoryRepository;
    }
    // make general function for post categories and get as well
    // use isDefault and isCategoriesEdited values to identify further steps
    // or use internal end points for default and user categories
    public BaseResponse<List<Category>> getCategories(String userId) {
        User user = userRepository.findById(userId)
                .orElse(null);
        BaseResponse<List<Category>> response;
        if (user == null) {
            logger.error(AppConstants.USER_NOT_FOUND);
            response = new BaseResponse<>(AppConstants.USER_NOT_FOUND, false, 404, null);
        }
        else{
            if (user.isCategoriesEdited()) {
                response = getCategoriesForUser(user);
            } else {
                response = getDefaultCategories();
            }
        }
        return response;
    }
    public BaseResponse<List<Category>> getCategoriesForUser(User user) {
        // we need to return default+user categories
        return new BaseResponse<>("User categories returned", true, 200, categoryRepository.findUserCategories(user.getId()));
    }
    public BaseResponse<List<Category>> getDefaultCategories() {
        List<Category> categories = categoryRepository.findDefaultCategories();
        return new BaseResponse<>("Default categories returned", true, 200, categories);
    }

    // not for user
    // also need to add in user categories table for all users - pending
    public BaseResponse<Category> createDefaultCategory(Category category) {
        category.setDefault(true);
        categoryRepository.save(category);
        BaseResponse<Category> response = new BaseResponse<>("Default category created successfully", true, 201, category);
        return response;
    }

    public BaseResponse<Category> updateUserCategory(String userId, Category category) {
        BaseResponse<Category> response;
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.error(AppConstants.USER_NOT_FOUND);
                response = new BaseResponse<>(AppConstants.USER_NOT_FOUND, false, 404, null);
            } else {
                // if user has not edited categories, then we need to add default categories to user categories
                if (!user.isCategoriesEdited()) {
                    copyDefaultCategoriesToUserCategories(user, category);
                }
                // find in user categories table
                Category existingCategory = categoryRepository.findById(String.valueOf(category.getId())).orElse(null);
                if (existingCategory == null) {
                    response = new BaseResponse<>("Category not found", false, 404, null);
                    return response;
                } else {
                    if(category.getName()!=null) existingCategory.setName(category.getName());
                    if(category.getColor()!=null) existingCategory.setColor(category.getColor());
                    if (category.getType()!=null) existingCategory.setType(category.getType());
                    categoryRepository.save(existingCategory);
                    userCategoryRepository.save(new UserCategory(new UserCategoryId(userId, category.getId()), user, existingCategory));
                    response = new BaseResponse<>("User category updated successfully", true, 200, existingCategory);
                }
            }
            return response;
        }
        catch (Exception e) {
            //some error occurred then need to do any rollback?
            logger.error("Error while creating user category", e);
            response = new BaseResponse<>("Error while creating user category", false, 500, null);
            response.setErrorDetails(new ErrorDetails(e.getMessage(),e.getCause().toString()));
            return response;
        }
    }
    public BaseResponse<Category> createUserCategory(String userId,Category category) {
        BaseResponse<Category> response;
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.error(AppConstants.USER_NOT_FOUND);
                response = new BaseResponse<>(AppConstants.USER_NOT_FOUND, false, 404, null);
            } else {
                // if user has not edited categories, then we need to add default categories to user categories
                if (!user.isCategoriesEdited()) {
                    copyDefaultCategoriesToUserCategories(user);
                }
                category.setDefault(false);
                categoryRepository.save(category); // check changes in category after save - do we see id? yes
                userCategoryRepository.save(new UserCategory(new UserCategoryId(userId, category.getId()), user, category));
                response = new BaseResponse<>("User category created successfully", true, 201, category);
            }
            return response;
        }
        catch (Exception e) {
            //some error occurred then need to do any rollback?
            logger.error("Error while creating user category", e);
            response = new BaseResponse<>("Error while creating user category", false, 500, null);
            response.setErrorDetails(new ErrorDetails(e.getMessage(),e.getCause().toString()));
            return response;
        }
    }

    // do we allow user to delete default category? - yes
    @Transactional
    public BaseResponse<String> deleteUserCategory(String userId, Long categoryId) {
        BaseResponse<String> response;
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.error(AppConstants.USER_NOT_FOUND);
                response = new BaseResponse<>(AppConstants.USER_NOT_FOUND, false, 404, null);
                return response;
            }
            if(!user.isCategoriesEdited()){
                // user has not edited any category yet so first we will copy all default categories to user categories
                // then we delete category and make categories edited true and save the user
                Category dummy = new Category(); //when user tries to delete default category
                dummy.setId(categoryId);
                copyDefaultCategoriesToUserCategories(user,dummy);
                categoryId = dummy.getId();
            }
            userCategoryRepository.deleteUserCategoriesById(new UserCategoryId(userId, categoryId));
            categoryRepository.deleteById(String.valueOf(categoryId));
            response = new BaseResponse<>("Category deleted successfully", true, 200, null);
            return response;
        } catch (Exception e) {
            logger.error("Error while deleting category", e);
            response = new BaseResponse<>("Error while deleting category", false, 500, null);
            response.setErrorDetails(new ErrorDetails(e.getMessage(),e.getCause().toString()));
            return response;
        }
    }
    public BaseResponse<String> deleteDefaultCategory(String categoryId) {
        BaseResponse<String> response;
        try {
            categoryRepository.deleteById(categoryId);
            response = new BaseResponse<>("Category deleted successfully", true, 200, null);
            return response;
        } catch (Exception e) {
            logger.error("Error while deleting category", e);
            response = new BaseResponse<>("Error while deleting category", false, 500, null);
            return response;
        }
    }
    public void copyDefaultCategoriesToUserCategories(User user) {
        List<Category> defaultCategories = categoryRepository.findDefaultCategories();
        //creating copy of default category in categories and user_categories table
        for (Category defaultCategory : defaultCategories) {
            Category usersDefaultCategory = new Category(null, defaultCategory.getType(), defaultCategory.getName(), defaultCategory.getColor(), defaultCategory.isDefault());
            categoryRepository.save(usersDefaultCategory);
            userCategoryRepository.save(new UserCategory(new UserCategoryId(user.getId(), usersDefaultCategory.getId()), user, usersDefaultCategory));
        }
        user.setCategoriesEdited(true);
        userRepository.save(user);
    }
    public void copyDefaultCategoriesToUserCategories(User user, Category category) {
        List<Category> defaultCategories = categoryRepository.findDefaultCategories();
        //creating copy of default category in categories and user_categories table
        for (Category defaultCategory : defaultCategories) {
            Category usersDefaultCategory = new Category(null, defaultCategory.getType(), defaultCategory.getName(), defaultCategory.getColor(), defaultCategory.isDefault());
            categoryRepository.save(usersDefaultCategory);
            if(Objects.equals(category.getId(), defaultCategory.getId())){
                category.setId(usersDefaultCategory.getId());
            }
            userCategoryRepository.save(new UserCategory(new UserCategoryId(user.getId(), usersDefaultCategory.getId()), user, usersDefaultCategory));
        }
        user.setCategoriesEdited(true);
        userRepository.save(user);
    }
}
