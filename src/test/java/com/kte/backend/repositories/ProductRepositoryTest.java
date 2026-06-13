package com.kte.backend.repositories;

import com.kte.backend.entities.Category;
import com.kte.backend.entities.Product;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ProductRepositoryTest.AuditingTestConfig.class)
class ProductRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing(auditorAwareRef = "testAuditor")
    static class AuditingTestConfig {
        @Bean
        AuditorAware<String> testAuditor() {
            return () -> Optional.of("test-user");
        }
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .name("Electronics")
                .descripotion("Electronic devices")
                .build();
        entityManager.persist(category);

        Product laptop = Product.builder()
                .name("Laptop Pro")
                .reference("LAP-001")
                .description("High-performance laptop")
                .alertThreshold(5)
                .price(BigDecimal.valueOf(1299.99))
                .category(category)
                .build();
        entityManager.persist(laptop);
        entityManager.flush();
    }

    @Test
    void findByNameIgnoreCase_shouldReturnProduct_whenNameMatchesExactCase() {
        Optional<Product> result = productRepository.findByNameIgnoreCase("Laptop Pro");

        assertThat(result).isPresent();
        assertThat(result.get().getReference()).isEqualTo("LAP-001");
    }

    @Test
    void findByNameIgnoreCase_shouldReturnProduct_whenNameIsLowercase() {
        Optional<Product> result = productRepository.findByNameIgnoreCase("laptop pro");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Laptop Pro");
    }

    @Test
    void findByNameIgnoreCase_shouldReturnProduct_whenNameIsUppercase() {
        Optional<Product> result = productRepository.findByNameIgnoreCase("LAPTOP PRO");

        assertThat(result).isPresent();
        assertThat(result.get().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(1299.99));
    }

    @Test
    void findByNameIgnoreCase_shouldReturnEmpty_whenNameNotFound() {
        Optional<Product> result = productRepository.findByNameIgnoreCase("Desktop");

        assertThat(result).isEmpty();
    }

    @Test
    void findByNameIgnoreCase_shouldReturnEmpty_whenNameIsPartialMatch() {
        Optional<Product> result = productRepository.findByNameIgnoreCase("Laptop");

        assertThat(result).isEmpty();
    }
}