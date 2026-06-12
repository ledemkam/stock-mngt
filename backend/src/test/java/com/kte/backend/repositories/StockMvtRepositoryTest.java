package com.kte.backend.repositories;

import com.kte.backend.entities.Category;
import com.kte.backend.entities.Product;
import com.kte.backend.entities.StockMvt;
import com.kte.backend.enums.TypeMvt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(StockMvtRepositoryTest.AuditingTestConfig.class)
class StockMvtRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing(auditorAwareRef = "testAuditor")
    static class AuditingTestConfig {
        @Bean
        AuditorAware<String> testAuditor() {
            return () -> Optional.of("test-user");
        }
    }

    @Autowired
    private StockMvtRepository stockMvtRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product product;
    private StockMvt inboundMvt;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .name("Electronics")
                .deleted(false)
                .build();
        entityManager.persist(category);

        product = Product.builder()
                .name("Laptop")
                .reference("LAPTOP-001")
                .description("Gaming laptop")
                .alertThreshold(5)
                .price(new BigDecimal("999.99"))
                .category(category)
                .deleted(false)
                .build();
        entityManager.persist(product);

        inboundMvt = StockMvt.builder()
                .quantity(10)
                .typeMvt(TypeMvt.IN)
                .mvtDate(LocalDateTime.of(2026, 1, 10, 9, 0))
                .comment("Initial stock")
                .product(product)
                .deleted(false)
                .build();
        entityManager.persist(inboundMvt);
        entityManager.flush();
    }

    // ---- save / findById ----

    @Test
    void save_shouldPersistStockMvt_andAssignId() {
        StockMvt mvt = StockMvt.builder()
                .quantity(5)
                .typeMvt(TypeMvt.OUT)
                .mvtDate(LocalDateTime.now())
                .product(product)
                .deleted(false)
                .build();

        StockMvt saved = stockMvtRepository.save(mvt);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualTo(5);
        assertThat(saved.getTypeMvt()).isEqualTo(TypeMvt.OUT);
    }

    @Test
    void findById_shouldReturnStockMvt_whenExists() {
        Optional<StockMvt> result = stockMvtRepository.findById(inboundMvt.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isEqualTo(10);
        assertThat(result.get().getTypeMvt()).isEqualTo(TypeMvt.IN);
        assertThat(result.get().getComment()).isEqualTo("Initial stock");
    }

    @Test
    void findById_shouldReturnEmpty_whenIdNotFound() {
        Optional<StockMvt> result = stockMvtRepository.findById("non-existent-id");

        assertThat(result).isEmpty();
    }

    // ---- findAll ----

    @Test
    void findAll_shouldReturnAllStockMvts() {
        List<StockMvt> result = stockMvtRepository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(inboundMvt.getId());
    }

    @Test
    void findAll_shouldReturnAllWhenMultipleExist() {
        StockMvt outboundMvt = StockMvt.builder()
                .quantity(3)
                .typeMvt(TypeMvt.OUT)
                .mvtDate(LocalDateTime.of(2026, 1, 15, 14, 0))
                .product(product)
                .deleted(false)
                .build();
        entityManager.persist(outboundMvt);
        entityManager.flush();

        List<StockMvt> result = stockMvtRepository.findAll();

        assertThat(result).hasSize(2);
    }

    // ---- existsById ----

    @Test
    void existsById_shouldReturnTrue_whenStockMvtExists() {
        assertThat(stockMvtRepository.existsById(inboundMvt.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_whenIdNotFound() {
        assertThat(stockMvtRepository.existsById("non-existent-id")).isFalse();
    }

    // ---- count ----

    @Test
    void count_shouldReturnCorrectTotal() {
        assertThat(stockMvtRepository.count()).isEqualTo(1);
    }

    @Test
    void count_shouldIncrementAfterSave() {
        StockMvt extra = StockMvt.builder()
                .quantity(2)
                .typeMvt(TypeMvt.OUT)
                .mvtDate(LocalDateTime.now())
                .product(product)
                .deleted(false)
                .build();
        entityManager.persist(extra);
        entityManager.flush();

        assertThat(stockMvtRepository.count()).isEqualTo(2);
    }

    // ---- delete ----

    @Test
    void deleteById_shouldRemoveStockMvt() {
        stockMvtRepository.deleteById(inboundMvt.getId());
        entityManager.flush();

        assertThat(stockMvtRepository.findById(inboundMvt.getId())).isEmpty();
    }

    @Test
    void delete_shouldRemoveStockMvt() {
        stockMvtRepository.delete(inboundMvt);
        entityManager.flush();

        assertThat(stockMvtRepository.existsById(inboundMvt.getId())).isFalse();
        assertThat(stockMvtRepository.count()).isZero();
    }

    // ---- product association ----

    @Test
    void findById_shouldReturnStockMvtWithProductLoaded() {
        entityManager.clear();

        Optional<StockMvt> result = stockMvtRepository.findById(inboundMvt.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getProduct().getId()).isEqualTo(product.getId());
        assertThat(result.get().getProduct().getName()).isEqualTo("Laptop");
        assertThat(result.get().getProduct().getReference()).isEqualTo("LAPTOP-001");
    }

    @Test
    void save_shouldAllowNullComment() {
        StockMvt mvtWithoutComment = StockMvt.builder()
                .quantity(7)
                .typeMvt(TypeMvt.IN)
                .mvtDate(LocalDateTime.now())
                .product(product)
                .deleted(false)
                .build();

        StockMvt saved = stockMvtRepository.save(mvtWithoutComment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getComment()).isNull();
    }
}