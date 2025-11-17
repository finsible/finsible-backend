package org.finsible.backend.controller;

import org.finsible.backend.BaseResponse;
import org.finsible.backend.entity.Category;
import org.finsible.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    private CategoryService categoryService;

    @Autowired
    public void setCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    // todo: fetch it based on transaction type
    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<Category>>> getCategories(@RequestAttribute("userId") String userId) {
       return categoryService.getCategories(userId);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<BaseResponse<List<Category>>> getCategoriesByType(@RequestAttribute("userId") String userId, @PathVariable Category.CategoryType type) {
        return categoryService.getCategoriesByType(userId, type);
    }

    // for admin use only
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/default")
    public ResponseEntity<BaseResponse<Category>> createDefaultCategory(@RequestBody Category category) {
        return categoryService.createDefaultCategory(category);
    }

    @PostMapping("/user")
    public ResponseEntity<BaseResponse<Category>> createUserCategory(@RequestAttribute("userId") String userId, @RequestBody Category category) {
        return categoryService.createUserCategory(userId, category);
    }

    @PutMapping("/user")
    public ResponseEntity<BaseResponse<Category>> updateUserCategory(@RequestAttribute("userId") String userId, @RequestBody Category category) {
        return categoryService.updateUserCategory(userId, category);
    }

    @DeleteMapping("/user/{categoryId}")
    public ResponseEntity<BaseResponse<Category>> deleteUserCategory(@RequestAttribute("userId") String userId, @PathVariable Long categoryId) {
        return categoryService.deleteUserCategory(userId, categoryId);
    }
}
