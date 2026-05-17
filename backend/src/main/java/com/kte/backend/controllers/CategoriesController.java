package com.kte.backend.controllers;


import com.kte.backend.controllers.uicontrollers.IUCategoryControllers;
import com.kte.backend.dto.requests.CategoryRequest;
import com.kte.backend.dto.responses.CategoryResponse;
import com.kte.backend.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/categories")
public class CategoriesController implements IUCategoryControllers{

    private  final CategoryService categoryService;


    @Override
    @PostMapping
    public ResponseEntity<Void> createCategory(
            @Valid
            @RequestBody
            final CategoryRequest request) {
        log.info("Received request to create category : {}", request);
        categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PutMapping("/{category-id}")
    public ResponseEntity<Void> updateCategory(
            @Valid
            @RequestBody
            @PathVariable("category-id")
            final  String id,
            final CategoryRequest request) {
        log.info("Received request to update category with id {}: {}", id, request);
        categoryService.update(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(int page, int size) {
        return null;
    }

    @Override
    public ResponseEntity<CategoryResponse> getCategoryById(String id) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteCategory(String id) {
        return null;
    }


}
