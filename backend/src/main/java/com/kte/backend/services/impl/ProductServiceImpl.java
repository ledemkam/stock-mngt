package com.kte.backend.services.impl;


import com.kte.backend.common.PageReponse;
import com.kte.backend.entities.Category;
import com.kte.backend.entities.Product;
import com.kte.backend.mappers.ProductMapper;
import com.kte.backend.repositories.CategoryRepository;
import com.kte.backend.repositories.ProductRepository;
import com.kte.backend.requests.ProductRequest;
import com.kte.backend.responses.ProductResponse;
import com.kte.backend.services.CategoryService;
import com.kte.backend.services.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;


    @Override
    public void create(ProductRequest request) {
        checkIfProductAlreadyExistByReference(request.getReference());
        checkIfCategoryExistById(request.getCategoryId());
        final Product entity = productMapper.toEntity(request);
        log.info("Saving product: {}", entity);
        productRepository.save(entity);

    }

    @Override
    public void update(String id, ProductRequest request) {
        final Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isEmpty()) {
            log.debug("Product with id {} not found", id);
            throw new EntityNotFoundException("Product not found");
        }
        checkIfProductAlreadyExistByReference(request.getReference());
        checkIfCategoryExistById(request.getCategoryId());
        final Product productToUpdate = productMapper.toEntity(request);
        productToUpdate.setId(id);
        log.info("Updating product with id {}: {}", id, productToUpdate);
        productRepository.save(productToUpdate);

    }

    @Override
    public PageReponse<ProductResponse> findAll(int page, int size) {
        return null;
    }

    @Override
    public ProductResponse findById(String id) {
        return null;
    }

    @Override
    public void delete(String id) {

    }


    private void checkIfProductAlreadyExistByReference(final String reference) {
        final Product existingProduct = productRepository.findByNameIgnoreCase(reference)
                .orElseThrow(() ->{
                    log.debug("Product with reference {} already exists", reference);
                    return new RuntimeException("Product already exists");
                });
    }

    private void checkIfCategoryExistById(final String categoryId){
        final Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.debug("Category with id {} not found", categoryId);
                    return new EntityNotFoundException("Category not found");
                });

    }
}
