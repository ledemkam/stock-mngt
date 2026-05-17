package com.kte.backend.controllers;

import com.kte.backend.common.PageReponse;
import com.kte.backend.controllers.uicontrollers.UIProductController;
import com.kte.backend.dto.requests.ProductRequest;
import com.kte.backend.dto.responses.ProductResponse;
import com.kte.backend.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductController implements UIProductController {

    private final ProductService productService;


    @Override
    @PostMapping
    public ResponseEntity<Void> createProduct(
            @Valid
            @RequestBody
            final ProductRequest request) {
        log.info("Received request to create product : {}", request);
        productService.create(request);
        log.info("Product created successfully: {}", request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PutMapping(path = "/{product-id}")
    public ResponseEntity<Void> updateProduct(
            @Valid
            @RequestBody
            @PathVariable("product-id")
            final String id,
            final ProductRequest request) {
        log.info("Received request to update product with id {}: {}", id, request);
        productService.update(id, request);
        log.info("Product with id {} updated successfully: {}", id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    @GetMapping
    public ResponseEntity<PageReponse<ProductResponse>> getAllProducts(
            @RequestParam(name="page" , defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            int size) {
        return ResponseEntity.ok(productService.findAll(page, size));
    }


    @Override
    @GetMapping(path = "/{product-id}")
    public ResponseEntity<ProductResponse> getCategoryById(
            @PathVariable("product-id")
            final String id) {
        log.debug("Received request to get product with id {}", id);
        return ResponseEntity.ok(productService.findById(id));
    }

    @Override
    @DeleteMapping(path = "/{product-id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable("product-id")
            final String id) {
        log.info("Received request to delete product with id {}", id);
        productService.delete(id);
        log.info("Product with id {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}
