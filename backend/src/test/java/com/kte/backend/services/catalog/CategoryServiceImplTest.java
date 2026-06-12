package com.kte.backend.services.catalog;

import com.kte.backend.common.PageReponse;
import com.kte.backend.dto.requests.CategoryRequest;
import com.kte.backend.dto.responses.CategoryResponse;
import com.kte.backend.entities.Category;
import com.kte.backend.exceptions.DuplicateEntityException;
import com.kte.backend.mappers.CategoryMapper;
import com.kte.backend.repositories.CategoryRepository;
import com.kte.backend.services.catalog.impl.CategoryServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryRequest request;
    private Category category;
    private CategoryResponse response;

    @BeforeEach
    void setUp() {
        request = CategoryRequest.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();

        category = Category.builder()
                .name("Electronics")
                .descripotion("Electronic devices")
                .build();
        category.setId("cat-001");

        response = CategoryResponse.builder()
                .name("Electronics")
                .description("Electronic devices")
                .nbProducts(0)
                .build();
    }

    // ---- create ----

    @Test
    void create_shouldSaveCategory_whenNameDoesNotExist() {
        when(categoryRepository.findByNameIgnoreCase(request.getName())).thenReturn(Optional.empty());
        when(categoryMapper.toEntity(request)).thenReturn(category);

        categoryService.create(request);

        verify(categoryRepository).save(category);
    }

    @Test
    void create_shouldThrowDuplicateEntityException_whenNameAlreadyExists() {
        when(categoryRepository.findByNameIgnoreCase(request.getName())).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Category already exists");

        verify(categoryRepository, never()).save(any());
    }

    // ---- update ----

    @Test
    void update_shouldUpdateCategory_whenIdExistsAndNameIsUnique() {
        when(categoryRepository.findById("cat-001")).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase(request.getName())).thenReturn(Optional.empty());
        when(categoryMapper.toEntity(request)).thenReturn(category);

        categoryService.update("cat-001", request);

        verify(categoryRepository).save(category);
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenIdNotFound() {
        when(categoryRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update("unknown-id", request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowDuplicateEntityException_whenNameAlreadyExists() {
        Category other = Category.builder().name("Electronics").build();
        other.setId("cat-999");

        when(categoryRepository.findById("cat-001")).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase(request.getName())).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> categoryService.update("cat-001", request))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Category already exists");

        verify(categoryRepository, never()).save(any());
    }

    // ---- findById ----

    @Test
    void findById_shouldReturnCategoryResponse_whenIdExists() {
        when(categoryRepository.findById("cat-001")).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.findById("cat-001");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electronics");
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenIdNotFound() {
        when(categoryRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById("unknown-id"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    // ---- findAll ----

    @Test
    void findAll_shouldReturnPagedResponse() {
        Page<Category> categoryPage = new PageImpl<>(List.of(category), PageRequest.of(0, 10), 1);
        when(categoryRepository.findAll(any(PageRequest.class))).thenReturn(categoryPage);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        PageReponse<CategoryResponse> result = categoryService.findAll(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ---- delete ----

    @Test
    void delete_shouldDeleteCategory_whenIdExists() {
        when(categoryRepository.findById("cat-001")).thenReturn(Optional.of(category));

        categoryService.delete("cat-001");

        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenIdNotFound() {
        when(categoryRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete("unknown-id"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found");

        verify(categoryRepository, never()).delete(any());
    }
}