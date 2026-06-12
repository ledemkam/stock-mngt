package com.kte.backend.services.tenant.impl;

import com.kte.backend.entities.Tenant;
import com.kte.backend.exceptions.TenantProvisioningException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvisioningServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DataSource dataSource;

    @InjectMocks
    private ProvisioningServiceImpl provisioningService;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId("tenant-001");
        tenant.setCompagnyName("Acme Corp");
        tenant.setCompagnyCode("ACME");
    }

    // ---- provisionTenant — rollback paths ----

    @Test
    void provisionTenant_shouldThrowTenantProvisioningException_andDropSchema_whenSchemaCreationFails() {
        // jdbcTemplate est mocké : on force une erreur sur CREATE SCHEMA
        doThrow(new RuntimeException("DB connection error"))
                .when(jdbcTemplate).execute(argThat((String sql) -> sql.contains("CREATE SCHEMA")));

        assertThatThrownBy(() -> provisioningService.provisionTenant(tenant))
                .isInstanceOf(TenantProvisioningException.class)
                .hasMessageContaining("Failed to provision tenant");

        // rollback : DROP SCHEMA doit être appelé
        verify(jdbcTemplate).execute(argThat((String sql) -> sql.contains("DROP SCHEMA")));
    }

    @Test
    void provisionTenant_shouldThrowTenantProvisioningException_andDropSchema_whenMigrationFails() {
        // CREATE SCHEMA réussit (no-op sur mock), mais Flyway échoue car DataSource est mocké
        // (getConnection() retourne null → NullPointerException interceptée)
        assertThatThrownBy(() -> provisioningService.provisionTenant(tenant))
                .isInstanceOf(TenantProvisioningException.class)
                .hasMessageContaining("Failed to provision tenant");

        // CREATE SCHEMA a bien été tenté
        verify(jdbcTemplate).execute(argThat((String sql) -> sql.contains("CREATE SCHEMA")));
        // rollback : DROP SCHEMA a bien été appelé
        verify(jdbcTemplate).execute(argThat((String sql) -> sql.contains("DROP SCHEMA")));
    }

    // ---- schéma name generation ----

    @Test
    void provisionTenant_shouldGenerateSchemaName_prefixedWithTenantUnderscore() {
        // "Acme Corp" → tenant_acme_corp
        assertThatThrownBy(() -> provisioningService.provisionTenant(tenant))
                .isInstanceOf(TenantProvisioningException.class);

        verify(jdbcTemplate).execute("CREATE SCHEMA IF NOT EXISTS tenant_acme_corp");
    }

    @Test
    void provisionTenant_shouldReplaceSpecialCharsInSchemaName_withUnderscore() {
        // "My-Corp & Co!" → my_corp___co_ → my_corp_co_ (greedy [^a-z0-9]+)
        tenant.setCompagnyName("My-Corp & Co!");

        assertThatThrownBy(() -> provisioningService.provisionTenant(tenant))
                .isInstanceOf(TenantProvisioningException.class);

        verify(jdbcTemplate).execute("CREATE SCHEMA IF NOT EXISTS tenant_my_corp_co_");
    }

    @Test
    void provisionTenant_shouldUseLowercaseSchemaName() {
        tenant.setCompagnyName("UPPERCASE COMPANY");

        assertThatThrownBy(() -> provisioningService.provisionTenant(tenant))
                .isInstanceOf(TenantProvisioningException.class);

        verify(jdbcTemplate).execute("CREATE SCHEMA IF NOT EXISTS tenant_uppercase_company");
    }

    // ---- SQL généré ----

    @Test
    void provisionTenant_shouldExecuteDropSchemaWithCascade_onRollback() {
        doThrow(new RuntimeException("error"))
                .when(jdbcTemplate).execute(argThat((String sql) -> sql.contains("CREATE SCHEMA")));

        assertThatThrownBy(() -> provisioningService.provisionTenant(tenant))
                .isInstanceOf(TenantProvisioningException.class);

        // vérifie que CASCADE est présent dans le DROP pour supprimer proprement le schéma
        verify(jdbcTemplate).execute(argThat((String sql) ->
                sql.contains("DROP SCHEMA IF EXISTS") && sql.contains("CASCADE")));
    }
}