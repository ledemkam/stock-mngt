package com.kte.backend.repositories;

import com.kte.backend.entities.Tenant;
import com.kte.backend.enums.TenantStatus;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TenantRepositoryTest.AuditingTestConfig.class)
class TenantRepositoryTest {

    // Tenant n'a pas @EntityListeners, donc createdAt doit être défini manuellement.
    @TestConfiguration
    @EnableJpaAuditing(auditorAwareRef = "testAuditor")
    static class AuditingTestConfig {
        @Bean
        AuditorAware<String> testAuditor() {
            return () -> Optional.of("test-user");
        }
    }

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        Tenant tenant = buildTenant("Acme Corp", "ACME", "contact@acme.com", "admin@acme.com", "acmeadmin");
        entityManager.persist(tenant);
        entityManager.flush();
    }

    // ---- existsByCompagnyCode ----

    @Test
    void existsByCompagnyCode_shouldReturnTrue_whenCodeExists() {
        assertThat(tenantRepository.existsByCompagnyCode("ACME")).isTrue();
    }

    @Test
    void existsByCompagnyCode_shouldReturnFalse_whenCodeNotFound() {
        assertThat(tenantRepository.existsByCompagnyCode("UNKNOWN")).isFalse();
    }

    @Test
    void existsByCompagnyCode_shouldReturnFalse_whenCodeIsEmpty() {
        assertThat(tenantRepository.existsByCompagnyCode("")).isFalse();
    }

    // ---- existsByEmail ----

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        assertThat(tenantRepository.existsByEmail("contact@acme.com")).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailNotFound() {
        assertThat(tenantRepository.existsByEmail("unknown@corp.com")).isFalse();
    }

    @Test
    void existsByEmail_shouldReturnFalse_afterTenantIsRemoved() {
        Tenant otherTenant = buildTenant("Beta Corp", "BETA", "contact@beta.com", "admin@beta.com", "betaadmin");
        entityManager.persist(otherTenant);
        entityManager.flush();

        tenantRepository.deleteById(otherTenant.getId());
        entityManager.flush();

        assertThat(tenantRepository.existsByEmail("contact@beta.com")).isFalse();
    }

    // ---- save / findById ----

    @Test
    void save_shouldPersistTenantAndGenerateId() {
        Tenant newTenant = buildTenant("Gamma Corp", "GAMMA", "contact@gamma.com", "admin@gamma.com", "gammaadmin");

        Tenant saved = tenantRepository.save(newTenant);

        assertThat(saved.getId()).isNotNull();
        assertThat(tenantRepository.findById(saved.getId())).isPresent();
    }

    private Tenant buildTenant(String compagnyName, String code, String email, String adminEmail, String adminUserName) {
        return Tenant.builder()
                .compagnyName(compagnyName)
                .compagnyCode(code)
                .email(email)
                .status(TenantStatus.PENDING)
                .adminFullName("Admin User")
                .adminEmail(adminEmail)
                .adminUserName(adminUserName)
                .adminPassword("encoded-password")
                .createdAt(LocalDateTime.now())
                .build();
    }
}