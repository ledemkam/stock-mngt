package com.kte.backend.controllers;


import com.kte.backend.controllers.uicontrollers.IUCategoryControllers;
import com.kte.backend.dto.requests.CategoryRequest;
import com.kte.backend.services.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/categories")
public class CategoriesController implements IUCategoryControllers{

    private  final CategoryService categoryService;


    @Override
    @PostMapping
    public ResponseEntity<Void> createCategory(final CategoryRequest request) {
        log.info("Received request to create category : {}", request);
        categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
