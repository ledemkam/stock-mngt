package com.kte.backend.mappers;

import com.kte.backend.entities.Category;
import com.kte.backend.dto.requests.CategoryRequest;
import com.kte.backend.dto.responses.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(final CategoryRequest request) {
       return Category.builder()
               .name(request.getName())
               .descripotion(request.getDescription())
               .build();
    }


    public CategoryResponse toResponse(final Category entity) {
        final int nbProduct = 0 ;// to be later implemented
        return CategoryResponse.builder()
                .name(entity.getName())
                .description(entity.getDescripotion())
                .nbProducts(nbProduct)
                .build();
    }
}
