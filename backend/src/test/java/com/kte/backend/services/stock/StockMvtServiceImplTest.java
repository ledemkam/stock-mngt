package com.kte.backend.services.stock;

import com.kte.backend.common.PageReponse;
import com.kte.backend.dto.requests.StockMvtRequest;
import com.kte.backend.dto.responses.StockMvtResponse;
import com.kte.backend.entities.Product;
import com.kte.backend.entities.StockMvt;
import com.kte.backend.enums.TypeMvt;
import com.kte.backend.mappers.StockMvtMapper;
import com.kte.backend.repositories.ProductRepository;
import com.kte.backend.repositories.StockMvtRepository;
import com.kte.backend.services.stock.impl.StockMvtServiceImpl;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMvtServiceImplTest {

    @Mock
    private StockMvtRepository stockMvtRepository;

    @Mock
    private StockMvtMapper stockMvtMapper;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockMvtServiceImpl stockMvtService;

    private StockMvtRequest request;
    private StockMvt stockMvt;
    private StockMvtResponse response;
    private Product product;

    @BeforeEach
    void setUp() {
        request = StockMvtRequest.builder()
                .quantity(10)
                .typeMvt(TypeMvt.IN)
                .mvtDate(LocalDateTime.now())
                .comment("Initial stock")
                .productId("prod-001")
                .build();

        product = Product.builder().name("Laptop").build();
        product.setId("prod-001");

        stockMvt = StockMvt.builder()
                .quantity(10)
                .typeMvt(TypeMvt.IN)
                .mvtDate(request.getMvtDate())
                .comment("Initial stock")
                .product(product)
                .build();
        stockMvt.setId("mvt-001");

        response = StockMvtResponse.builder()
                .quantity(10)
                .typeMvt(TypeMvt.IN)
                .mvtDate(request.getMvtDate())
                .comment("Initial stock")
                .build();
    }

    // ---- create ----

    @Test
    void create_shouldSaveStockMvt_whenProductExists() {
        when(productRepository.findById(request.getProductId())).thenReturn(Optional.of(product));
        when(stockMvtMapper.toEntity(request)).thenReturn(stockMvt);

        stockMvtService.create(request);

        verify(stockMvtRepository).save(stockMvt);
    }

    @Test
    void create_shouldThrowEntityNotFoundException_whenProductNotFound() {
        when(productRepository.findById(request.getProductId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockMvtService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(stockMvtRepository, never()).save(any());
    }

    // ---- update ----

    @Test
    void update_shouldUpdateStockMvt_whenIdExistsAndProductExists() {
        when(stockMvtRepository.findById("mvt-001")).thenReturn(Optional.of(stockMvt));
        when(productRepository.findById(request.getProductId())).thenReturn(Optional.of(product));
        when(stockMvtMapper.toEntity(request)).thenReturn(stockMvt);

        stockMvtService.update("mvt-001", request);

        verify(stockMvtRepository).save(stockMvt);
        assertThat(stockMvt.getId()).isEqualTo("mvt-001");
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenStockMvtNotFound() {
        when(stockMvtRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockMvtService.update("unknown-id", request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Stock movement not found");

        verify(stockMvtRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenProductNotFound() {
        when(stockMvtRepository.findById("mvt-001")).thenReturn(Optional.of(stockMvt));
        when(productRepository.findById(request.getProductId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockMvtService.update("mvt-001", request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(stockMvtRepository, never()).save(any());
    }

    // ---- findById ----

    @Test
    void findById_shouldReturnStockMvtResponse_whenIdExists() {
        when(stockMvtRepository.findById("mvt-001")).thenReturn(Optional.of(stockMvt));
        when(stockMvtMapper.toResponse(stockMvt)).thenReturn(response);

        StockMvtResponse result = stockMvtService.findById("mvt-001");

        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(result.getTypeMvt()).isEqualTo(TypeMvt.IN);
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenIdNotFound() {
        when(stockMvtRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockMvtService.findById("unknown-id"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Stock movement not found");
    }

    // ---- findAll ----

    @Test
    void findAll_shouldReturnPagedResponse() {
        Page<StockMvt> mvtPage = new PageImpl<>(List.of(stockMvt), PageRequest.of(0, 10), 1);
        when(stockMvtRepository.findAll(any(PageRequest.class))).thenReturn(mvtPage);
        when(stockMvtMapper.toResponse(stockMvt)).thenReturn(response);

        PageReponse<StockMvtResponse> result = stockMvtService.findAll(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ---- delete ----

    @Test
    void delete_shouldDeleteStockMvt_whenIdExists() {
        when(stockMvtRepository.findById("mvt-001")).thenReturn(Optional.of(stockMvt));

        stockMvtService.delete("mvt-001");

        verify(stockMvtRepository).delete(stockMvt);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenIdNotFound() {
        when(stockMvtRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockMvtService.delete("unknown-id"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Stock movement not found");

        verify(stockMvtRepository, never()).delete(any());
    }
}