package org.finsible.backend.controller;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.dto.request.CategoryRequestDTO;
import org.finsible.backend.dto.request.groups.Create;
import org.finsible.backend.dto.request.groups.Update;
import org.finsible.backend.dto.response.CategoryResponseDTO;
import org.finsible.backend.entity.Category;
import org.finsible.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<CategoryResponseDTO>>> getCategories(@RequestAttribute("userId") String userId) {
       return ResponseEntity.ok(new BaseResponse<>("Categories fetched successfully", true, categoryService.getAllCategories(userId)));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<BaseResponse<List<CategoryResponseDTO>>> getCategoriesByType(@RequestAttribute("userId") String userId, @PathVariable Category.CategoryType type) {
        return ResponseEntity.ok(new BaseResponse<>("Categories of type " + type + " fetched successfully", true, categoryService.getCategoriesByType(userId, type)));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/default/")
    public ResponseEntity<BaseResponse<CategoryResponseDTO>> createDefaultCategory(@Validated(Create.class) @RequestBody CategoryRequestDTO categoryRequestDTO)
            throws BadRequestException {
        return ResponseEntity.ok(new BaseResponse<>("Default category created successfully", true, categoryService.createDefaultCategory(categoryRequestDTO)));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/default/{id}")
    public ResponseEntity<BaseResponse<CategoryResponseDTO>> updateDefaultCategory(@PathVariable Long id, @Validated(Update.class) @RequestBody CategoryRequestDTO categoryRequestDTO)
            throws BadRequestException {
        return ResponseEntity.ok(new BaseResponse<>("Default category updated successfully", true, categoryService.updateDefaultCategory(id, categoryRequestDTO)));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/default/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteDefaultCategory(@PathVariable Long id) {
        categoryService.deleteDefaultCategory(id);
        return ResponseEntity.ok(new BaseResponse<>("Default category deleted successfully", true));
    }

    @PostMapping("/")
    public ResponseEntity<BaseResponse<CategoryResponseDTO>> createUserCategory(@RequestAttribute("userId") String userId, @Validated(Create.class) @RequestBody CategoryRequestDTO categoryRequestDTO)
            throws BadRequestException {
        return ResponseEntity.ok(new BaseResponse<>("Category created successfully", true, categoryService.createUserCategory(userId, categoryRequestDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryResponseDTO>> updateUserCategory(@RequestAttribute("userId") String userId, @PathVariable Long id, @Validated(Update.class) @RequestBody CategoryRequestDTO categoryRequestDTO)
            throws BadRequestException {
        return ResponseEntity.ok(new BaseResponse<>("Category updated successfully", true, categoryService.updateUserCategory(userId, id, categoryRequestDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteUserCategory(@RequestAttribute("userId") String userId, @PathVariable Long id) {
        categoryService.deleteUserCategory(userId, id);
        return ResponseEntity.ok(new BaseResponse<>("Category deleted successfully", true));
    }
}
