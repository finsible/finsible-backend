package org.finsible.backend.authentication.controller;

import org.finsible.backend.BaseResponse;
import org.finsible.backend.authentication.entity.Category;
import org.finsible.backend.authentication.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    private CategoryService categoryService;

    @Autowired
    public void  setCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    // fetch it based on transaction type
    @GetMapping("/all")
    public BaseResponse<List<Category>> getCategories(@RequestAttribute("userId") String userId) {
       return categoryService.getCategories(userId);
    }

    @PostMapping("/default")
    public BaseResponse<Category> createDefaultCategory(@RequestBody Category category) {
        return categoryService.createDefaultCategory(category);
    }

    @PostMapping("/user")
    public BaseResponse<Category> createUserCategory(@RequestAttribute("userId") String userId, @RequestBody Category category) {
        return categoryService.createUserCategory(userId, category);
    }

    @PutMapping("/user")
    public BaseResponse<Category> updateUserCategory(@RequestAttribute("userId") String userId, @RequestBody Category category) {
        return categoryService.updateUserCategory(userId, category);
    }

    @DeleteMapping("/user/{categoryId}")
    public BaseResponse<String> deleteUserCategory(@RequestAttribute("userId") String userId, @PathVariable Long categoryId) {
        return categoryService.deleteUserCategory(userId, categoryId);
    }
}
