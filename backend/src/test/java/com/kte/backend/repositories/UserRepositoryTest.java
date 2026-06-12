package com.kte.backend.repositories;

import com.kte.backend.entities.Tenant;
import com.kte.backend.entities.User;
import com.kte.backend.enums.TenantStatus;
import com.kte.backend.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserRepositoryTest.AuditingTestConfig.class)
class UserRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing(auditorAwareRef = "testAuditor")
    static class AuditingTestConfig {
        @Bean
        AuditorAware<String> testAuditor() {
            return () -> Optional.of("test-user");
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Tenant tenant;
    private User activeUser;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder()
                .compagnyName("Test Corp")
                .compagnyCode("TC001")
                .email("corp@test.com")
                .status(TenantStatus.ACTIVE)
                .adminFullName("Admin User")
                .adminEmail("admin@testcorp.com")
                .adminUserName("adminuser")
                .adminPassword("encoded-password")
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(tenant);

        activeUser = User.builder()
                .username("johndoe")
                .email("john.doe@test.com")
                .password("encoded-password")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.ROLE_USER)
                .enable(true)
                .deleted(false)
                .tenant(tenant)
                .build();
        entityManager.persist(activeUser);
        entityManager.flush();
    }

    // ---- findByIdAndNotDeleted ----

    @Test
    void findByIdAndNotDeleted_shouldReturnUser_whenUserExistsAndNotDeleted() {
        Optional<User> result = userRepository.findByIdAndNotDeleted(activeUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("johndoe");
    }

    @Test
    void findByIdAndNotDeleted_shouldReturnEmpty_whenUserIsDeleted() {
        User deletedUser = buildUser("deleted-user", "deleted@test.com", tenant, true);
        entityManager.persist(deletedUser);
        entityManager.flush();

        Optional<User> result = userRepository.findByIdAndNotDeleted(deletedUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndNotDeleted_shouldReturnEmpty_whenIdNotFound() {
        Optional<User> result = userRepository.findByIdAndNotDeleted("non-existent-id");

        assertThat(result).isEmpty();
    }

    // ---- findByUsername ----

    @Test
    void findByUsername_shouldReturnUser_whenUsernameExists() {
        Optional<User> result = userRepository.findByUsername("johndoe");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@test.com");
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenUsernameNotFound() {
        Optional<User> result = userRepository.findByUsername("unknown");

        assertThat(result).isEmpty();
    }

    // ---- existsByUsername ----

    @Test
    void existsByUsername_shouldReturnTrue_whenUsernameExists() {
        assertThat(userRepository.existsByUsername("johndoe")).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenUsernameNotFound() {
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    // ---- existsByEmail ----

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        assertThat(userRepository.existsByEmail("john.doe@test.com")).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailNotFound() {
        assertThat(userRepository.existsByEmail("nobody@test.com")).isFalse();
    }

    // ---- findAllByTenantId ----

    @Test
    void findAllByTenantId_shouldReturnActiveUsers_forGivenTenant() {
        Page<User> result = userRepository.findAllByTenantId(tenant.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("johndoe");
    }


    private User buildUser(String username, String email, Tenant userTenant, boolean deleted) {
        return User.builder()
                .username(username)
                .email(email)
                .password("encoded-password")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.ROLE_USER)
                .enable(!deleted)
                .tenant(userTenant)
                .deleted(deleted)
                .build();
    }
}