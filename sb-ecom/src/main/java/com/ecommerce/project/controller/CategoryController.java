package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/echo")
    public ResponseEntity<String> echoMessage(@RequestParam(name = "message", defaultValue = "Hello everyone") String message) {
        return new ResponseEntity<>("Echoed message: " + message, HttpStatus.OK);
    }

    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORY_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        return new ResponseEntity<>(categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder), HttpStatus.OK);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO savedCategory = categoryService.addCategory(categoryDTO);

        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED) ;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId) {
        CategoryDTO deleteCategory = categoryService.deleteCategory(categoryId);

        return new ResponseEntity<>(deleteCategory, HttpStatus.OK);
    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO, @PathVariable Long categoryId) {
        CategoryDTO savedCategory = categoryService.updateCategory(categoryDTO, categoryId);

        return new ResponseEntity<>(savedCategory, HttpStatus.OK);
    }
}
