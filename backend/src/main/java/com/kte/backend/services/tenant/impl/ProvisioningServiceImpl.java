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
        final String  schemaName = "tenant_" + tenant.getCompagnyName().toLowerCase();

        try{
            log.info("Provisioning tenant {} with schema name: {}",tenant.getCompagnyName(), schemaName);
            //1.Create the PostGres schema
            createSchema(schemaName);
            log.info("Schema {} created successfully for tenant {}", schemaName, tenant.getCompagnyName());

            //2.run Flyway migrations for this schema
            runTenantMigration(schemaName);
            log.info("Flyway migrations executed successfully for tenant {}",schemaName);

            //3.Initialize the default data(optionel)
            initialiazeDefaultData(schemaName,tenant);

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

    private void runTenantMigration(String schemaName) {
        log.info("Running Flyway migrations for tenant schema: {}", schemaName);
        final Flyway tenantFlyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations("classpath:db/migration/tenant")
                .baselineOnMigrate(true)
                .table("flyway_schema_history")
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load();

        log.info("Starting Flyway migration for tenant schema: {}", schemaName);
        tenantFlyway.migrate();
        log.info("Flyway migration completed for tenant schema: {}", schemaName);

    }

    private void createSchema(final  String schemaName){
        final String sql = String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName);
        jdbcTemplate.execute(sql);
    }

    private void initialiazeDefaultData(final String schemaName,final Tenant tenant){
        log.info("Initializing default data for tenant schema: {}", schemaName);
        // Example: Insert default settings for the tenant

    }

    private void dropSchema(final String schemaName){
        log.info("Dropping schema {} for tenant", schemaName);
        final String sql = String.format("DROP SCHEMA IF EXISTS %s CASCADE", schemaName);
        jdbcTemplate.execute(sql);
    }
}
