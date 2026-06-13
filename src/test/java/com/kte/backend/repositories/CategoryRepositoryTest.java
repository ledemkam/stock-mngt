package com.kte.backend.repositories;

import com.kte.backend.entities.Category;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CategoryRepositoryTest.AuditingTestConfig.class)
class CategoryRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing(auditorAwareRef = "testAuditor")
    static class AuditingTestConfig {
        @Bean
        AuditorAware<String> testAuditor() {
            return () -> Optional.of("test-user");
        }
    }

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        Category electronics = Category.builder()
                .name("Electronics")
                .descripotion("Electronic devices")
                .build();
        entityManager.persist(electronics);
        entityManager.flush();
    }

    @Test
    void findByNameIgnoreCase_shouldReturnCategory_whenNameMatchesExactCase() {
        Optional<Category> result = categoryRepository.findByNameIgnoreCase("Electronics");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
    }

    @Test
    void findByNameIgnoreCase_shouldReturnCategory_whenNameIsLowercase() {
        Optional<Category> result = categoryRepository.findByNameIgnoreCase("electronics");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
    }

    @Test
    void findByNameIgnoreCase_shouldReturnCategory_whenNameIsMixedCase() {
        Optional<Category> result = categoryRepository.findByNameIgnoreCase("ELECTRONICS");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
    }

    @Test
    void findByNameIgnoreCase_shouldReturnEmpty_whenNameNotFound() {
        Optional<Category> result = categoryRepository.findByNameIgnoreCase("Unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void findByNameIgnoreCase_shouldReturnEmpty_whenNameIsPartialMatch() {
        Optional<Category> result = categoryRepository.findByNameIgnoreCase("Electro");

        assertThat(result).isEmpty();
    }
}