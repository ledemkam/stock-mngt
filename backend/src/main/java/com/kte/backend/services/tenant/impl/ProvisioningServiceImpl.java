package com.kte.backend.services.tenant.impl;

import com.kte.backend.entities.Tenant;
import com.kte.backend.exceptions.TenantProvisioningException;
import com.kte.backend.services.tenant.ProvisioningService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class ProvisioningServiceImpl implements ProvisioningService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;


    @Override
    public void provisionTenant(Tenant tenant) {
        final String schemaName = "tenant_" + tenant.getCompagnyName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_");

        try{
            log.info("Provisioning tenant {} with schema name: {}",tenant.getCompagnyName(), schemaName);
            //1.Create the PostGres schema
            createSchema(schemaName);
            log.info("Schema {} created successfully for tenant {}", schemaName, tenant.getCompagnyName());

            //2.run Flyway migrations for this schema
            runTenantMigration(schemaName);
            log.info("Flyway migrations executed successfully for tenant {}",schemaName);

            //3.Initialize the default data(optionel)
            initializeDefaultData(schemaName,tenant);

        } catch (final  Exception e) {
            log.error("Failled provisioning tenant {}: {}", tenant.getCompagnyName(), e.getMessage(), e);

            //rollback: drop schema creation
            try{

            dropSchema(schemaName);
            }catch (final Exception exp){
                log.error("Failed to rollback tenant provisioning for tenant {}", tenant.getCompagnyName(), e);
            }
             throw new TenantProvisioningException("Failed to provision tenant: ");
        }

    }

    private void dropSchema(final String schemaName) {
        final String sql = String.format("DROP SCHEMA IF EXISTS %s CASCADE", schemaName);
        this.jdbcTemplate.execute(sql);
    }

    private void createSchema(final String schemaName) {
        final String sql = String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName);
        this.jdbcTemplate.execute(sql);
    }

    private void runTenantMigration(final String schemaName) {
        log.info("Running tenant migrations for schema: {}", schemaName);
        final Flyway tenantFlyway = Flyway.configure()
                .dataSource(this.dataSource)
                .schemas(schemaName)
                .locations("classpath:db/migration/tenant")
                .baselineOnMigrate(true)
                .table("flyway_schema_history")
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load();

        log.info("Tenant migrations started");
        tenantFlyway.migrate();
        log.info("Tenant migrations completed");
    }

    private void initializeDefaultData(final String schemaName, final Tenant tenant) {
        log.info("Initializing default data for tenant: {}", tenant.getCompagnyName(),schemaName);
        // here you can add default data initialization code
    }
}
