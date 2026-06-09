package com.kte.backend.services.catalog;

import com.kte.backend.common.PageReponse;
import com.kte.backend.dto.requests.ProductRequest;
import com.kte.backend.dto.responses.ProductResponse;
import com.kte.backend.entities.Category;
import com.kte.backend.entities.Product;
import com.kte.backend.exceptions.DuplicateEntityException;
import com.kte.backend.mappers.ProductMapper;
import com.kte.backend.repositories.CategoryRepository;
import com.kte.backend.repositories.ProductRepository;
import com.kte.backend.services.catalog.impl.ProductServiceImpl;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductRequest request;
    private Product product;
    private ProductResponse response;
    private Category category;

    @BeforeEach
    void setUp() {
        request = ProductRequest.builder()
                .name("Laptop")
                .reference("REF-001")
                .description("A powerful laptop")
                .alertThreshold(5)
                .price(BigDecimal.valueOf(999.99))
                .categoryId("cat-001")
                .build();

        category = Category.builder().name("Electronics").build();
        category.setId("cat-001");

        product = Product.builder()
                .name("Laptop")
                .reference("REF-001")
                .description("A powerful laptop")
                .alertThreshold(5)
                .price(BigDecimal.valueOf(999.99))
                .category(category)
                .build();
        product.setId("prod-001");

        response = ProductResponse.builder()
                .name("Laptop")
                .reference("REF-001")
                .description("A powerful laptop")
                .alertThreshold(5)
                .price(BigDecimal.valueOf(999.99))
                .categoryName("Electronics")
                .build();
    }

    // ---- create ----

    @Test
    void create_shouldSaveProduct_whenReferenceIsUniqueAndCategoryExists() {
        when(productRepository.findByNameIgnoreCase(request.getReference())).thenReturn(Optional.empty());
        when(categoryRepository.findById(request.getCategoryId())).thenReturn(Optional.of(category));
        when(productMapper.toEntity(request)).thenReturn(product);

        productService.create(request);

        verify(productRepository).save(product);
    }

    @Test
    void create_shouldThrowDuplicateEntityException_whenReferenceAlreadyExists() {
        when(productRepository.findByNameIgnoreCase(request.getReference())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Product already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowEntityNotFoundException_whenCategoryNotFound() {
        when(productRepository.findByNameIgnoreCase(request.getReference())).thenReturn(Optional.empty());
        when(categoryRepository.findById(request.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found");

        verify(productRepository, never()).save(any());
    }

    // ---- update ----

    @Test
    void update_shouldUpdateProduct_whenIdExistsAndReferenceIsUniqueAndCategoryExists() {
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(product));
        when(productRepository.findByNameIgnoreCase(request.getReference())).thenReturn(Optional.empty());
        when(categoryRepository.findById(request.getCategoryId())).thenReturn(Optional.of(category));
        when(productMapper.toEntity(request)).thenReturn(product);

        productService.update("prod-001", request);

        verify(productRepository).save(product);
        assertThat(product.getId()).isEqualTo("prod-001");
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenProductNotFound() {
        when(productRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update("unknown-id", request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowDuplicateEntityException_whenReferenceAlreadyExists() {
        Product other = Product.builder().reference("REF-001").build();
        other.setId("prod-999");

        when(productRepository.findById("prod-001")).thenReturn(Optional.of(product));
        when(productRepository.findByNameIgnoreCase(request.getReference())).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> productService.update("prod-001", request))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Product already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenCategoryNotFound() {
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(product));
        when(productRepository.findByNameIgnoreCase(request.getReference())).thenReturn(Optional.empty());
        when(categoryRepository.findById(request.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update("prod-001", request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found");

        verify(productRepository, never()).save(any());
    }

    // ---- findById ----

    @Test
    void findById_shouldReturnProductResponse_whenIdExists() {
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.findById("prod-001");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");
        assertThat(result.getReference()).isEqualTo("REF-001");
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenIdNotFound() {
        when(productRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById("unknown-id"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    // ---- findAll ----

    @Test
    void findAll_shouldReturnPagedResponse() {
        Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(response);

        PageReponse<ProductResponse> result = productService.findAll(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ---- delete ----

    @Test
    void delete_shouldDeleteProduct_whenIdExists() {
        when(productRepository.findById("prod-001")).thenReturn(Optional.of(product));

        productService.delete("prod-001");

        verify(productRepository).delete(product);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenIdNotFound() {
        when(productRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete("unknown-id"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, never()).delete(any());
    }
}