package com.kte.backend.controllers;


import com.kte.backend.common.PageReponse;
import com.kte.backend.controllers.uicontrollers.UICategoryControllers;
import com.kte.backend.dto.requests.CategoryRequest;
import com.kte.backend.dto.responses.CategoryResponse;
import com.kte.backend.services.catalog.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/categories")
public class CategorieController implements UICategoryControllers {

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
    @PutMapping(path = "/{category-id}")
    public ResponseEntity<Void> updateCategory(
            @Valid
            @NotNull(message = "Category ID must not be null")
            @RequestBody
            @PathVariable("category-id")
            final  String id,
            final CategoryRequest request) {
        log.info("Received request to update category with id {}: {}", id, request);
        categoryService.update(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    @GetMapping(path = "/{category-id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @NotNull(message = "Category ID must not be null")
            @PathVariable("category-id")
            final String id) {
        log.debug("Received request to get category with id {}", id);
        return  ResponseEntity.ok(categoryService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<PageReponse<CategoryResponse>> getAllCategories(
            @RequestParam(name = "page", defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            final int size
             ) {
        log.debug("Received request to get all categories with page {} and size {}", page, size);
        return ResponseEntity.ok(categoryService.findAll(page, size));
    }


    @Override
    @DeleteMapping(path = "/{category-id}")
    public ResponseEntity<Void> deleteCategory(
            @NotNull(message = "Category ID must not be null")
            @PathVariable("category-id")
            final String id) {
        log.info("Received request to delete category with id {}", id);
        categoryService.delete(id);
        log.info("Successfully deleted category with id {}", id);
        return ResponseEntity.noContent().build();

    }


}
