package com.kte.backend.tenant;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** * Test d'intégration qui exécute les migrations "common" et "tenant" * et vérifie la création des tables du script tenant/V1__Init_DB_FOR_Tenant.sql. * * Exigence : Docker doit être en marche pour exécuter Testcontainers. */
@Testcontainers
@DisabledIfSystemProperty(named = "skip.integration.tests", matches = "true")
public class TenantMigrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("tenantdb")
            .withUsername("sa")
            .withPassword("sa");

    @Test
    void tenantMigrations_areApplied_andTablesExist() throws Exception {
        // Configure et exécute Flyway sur les locations common + tenant
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration/common", "classpath:db/migration/tenant")
                .load();

        flyway.migrate(); // applique les migrations

        // Vérifier que les tables attendues existent dans la BD
        try (Connection conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            DatabaseMetaData meta = conn.getMetaData();

            // Les noms des tables dans vos scripts sont : categories, products, stock_mvts
            assertTableExists(meta, "categories");
            assertTableExists(meta, "products");
            assertTableExists(meta, "stock_mvts");
        }
    }

    private static void assertTableExists(DatabaseMetaData meta, String tableName) throws Exception {
        // Postgres convertit souvent les identifiants en minuscules non-quotés
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            assertTrue(rs.next(), "Table '" + tableName + "' should exist after migrations");
        }
    }
}
