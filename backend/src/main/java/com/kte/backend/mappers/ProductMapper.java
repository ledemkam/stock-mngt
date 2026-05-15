package com.kte.backend.mappers;

import com.kte.backend.entities.Category;
import com.kte.backend.entities.Product;
import com.kte.backend.requests.ProductRequest;
import com.kte.backend.responses.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(final ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .reference(request.getReference())
                .description(request.getDescription())
                .price(request.getPrice())
                .alertThreshold(request.getAlertThreshold())
                .category(Category.builder()
                                  .id(request.getCategoryId())
                                  .build())
                .build();
    }

    public ProductResponse toResponse(final Product entity) {
        return ProductResponse.builder()
                .name(entity.getName())
                .reference(entity.getReference())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .alertThreshold(entity.getAlertThreshold())
                .categoryName(entity.getCategory().getName())
                //.availableQuantity() to be later implemented
                .build();
    }
}
