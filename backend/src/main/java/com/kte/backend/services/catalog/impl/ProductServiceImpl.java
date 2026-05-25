package com.kte.backend.services.catalog.impl;


import com.kte.backend.common.PageReponse;
import com.kte.backend.entities.Product;
import com.kte.backend.exceptions.DuplicateEntityException;
import com.kte.backend.mappers.ProductMapper;
import com.kte.backend.repositories.CategoryRepository;
import com.kte.backend.repositories.ProductRepository;
import com.kte.backend.dto.requests.ProductRequest;
import com.kte.backend.dto.responses.ProductResponse;
import com.kte.backend.services.catalog.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;


    @Override
    public void create(final ProductRequest request) {
        checkIfProductAlreadyExistByReference(request.getReference());
        checkIfCategoryExistById(request.getCategoryId());
        final Product entity = productMapper.toEntity(request);
        log.info("Saving product: {}", entity);
        productRepository.save(entity);

    }

    @Override
    public void update(final String id,final  ProductRequest request) {
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
    public PageReponse<ProductResponse> findAll(final int page, final int size) {
        log.debug("Finding all products with page {} and size {}", page, size);
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Product> products= productRepository.findAll(pageRequest);
        final Page<ProductResponse> productResponses = products.map(productMapper::toResponse);
        log.debug("Found {} products", productResponses.getTotalElements());
        return PageReponse.of(productResponses);
    }

    @Override
    public ProductResponse findById(final String id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> {
                    log.debug("Product with id {} not found", id);
                    return new EntityNotFoundException("Product not found");
                });
    }

    @Override
    public void delete(final String id) {
        final Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug("Product with id {} not found", id);
                    return new EntityNotFoundException("Product not found");
                });
        log.info("Deleting product with id {}: {}", id, existingProduct);
        productRepository.delete(existingProduct);

    }


    private void checkIfProductAlreadyExistByReference(final String reference) {
        productRepository.findByNameIgnoreCase(reference).ifPresent(p -> {
            log.debug("Product with reference {} already exists", reference);
            throw new DuplicateEntityException("Product already exists");
        });
    }

    private void checkIfCategoryExistById(final String categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.debug("Category with id {} not found", categoryId);
                    return new EntityNotFoundException("Category not found");
                });
    }
}
