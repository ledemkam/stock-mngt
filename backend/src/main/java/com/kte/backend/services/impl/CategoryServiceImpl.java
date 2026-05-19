package com.kte.backend.services.impl;

import com.kte.backend.common.PageReponse;
import com.kte.backend.entities.Category;
import com.kte.backend.exceptions.DuplicateCategoryException;
import com.kte.backend.mappers.CategoryMapper;
import com.kte.backend.repositories.CategoryRepository;
import com.kte.backend.dto.requests.CategoryRequest;
import com.kte.backend.dto.responses.CategoryResponse;
import com.kte.backend.services.CategoryService;
import jakarta.persistence.EntityExistsException;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    public void create(final CategoryRequest request) {
         checkIfCategoryAlreadyExistsByName(request.getName());
         final Category entity = categoryMapper.toEntity(request);
         log.info("Saving category: {}", entity);
         categoryRepository.save(entity);
    }

    @Override
    public void update(final String id, final CategoryRequest request) {
        final Optional<Category> existingCategory = categoryRepository.findById(id);
        if (existingCategory.isEmpty()) {
            log.debug("Category with id {} not found", id);
            throw new EntityNotFoundException("Category not found");
        }
        checkIfCategoryAlreadyExistsByName(request.getName());
        final Category categoryToUpdate = categoryMapper.toEntity(request);
        categoryToUpdate.setId(id);
        log.info("Updating category with id {}: {}", id, categoryToUpdate);
        categoryRepository.save(categoryToUpdate);
    }



    @Override
    public PageReponse<CategoryResponse> findAll(final int page, final int size) {
        log.debug("Finding all categories with page {} and size {}", page, size);
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Category> categories = categoryRepository.findAll(pageRequest);
        final Page<CategoryResponse> categoryResponses = categories.map(categoryMapper::toResponse);
        log.debug("Found {} categories", categoryResponses.getTotalElements());
        return PageReponse.of(categoryResponses);
    }

    @Override
    public CategoryResponse findById(final String id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> {
                    log.debug("Category with id {} not found", id);
                    return new EntityNotFoundException("Category not found");
                });
    }

    @Override
    public void delete(final String id) {
        final Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() ->new EntityNotFoundException("Category not found"));
        log.info("Deleting category with id {}: {}", id, existingCategory);
        categoryRepository.delete(existingCategory);

    }

    private void checkIfCategoryAlreadyExistsByName(final String categoryName){
        final Optional<Category> category = categoryRepository.findByNameIgnoreCase(categoryName);
        log.info("Category found with name {}: {}", categoryName, category);
        if (category.isPresent()) {
            log.debug("Category with name {} already exists", categoryName);
            throw new DuplicateCategoryException("Category already exists");
        }
    }
}


