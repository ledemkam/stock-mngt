package com.kte.backend.config.tenantConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {

    private final DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(final  Connection connection) throws SQLException {
       connection.close();
    }

    @Override
    public Connection getConnection(final String tenantIdentifier) throws SQLException {
        log.debug("Getting connection for tenant: {}", tenantIdentifier);
         final Connection connection = getAnyConnection();
         try {
             if (tenantIdentifier != null && !tenantIdentifier.equals("public")) {
             connection.createStatement().execute("SET search_path TO " + tenantIdentifier + ", public");
             log.trace("Set search_path to {} for tenant {}", tenantIdentifier, tenantIdentifier);
             }
         } catch (final SQLException e) {
             log.error("Error setting search_path for tenant {}: {}", tenantIdentifier, e.getMessage());
             throw e;
         }
        return connection;
    }

    @Override
    public void releaseConnection(final String tenantIdentifier, Connection connection) throws SQLException {
        try {
                connection.createStatement().execute("SET search_path TO public");

        } catch (final SQLException e) {
            log.error("Error getting connection for tenant {}: {}", tenantIdentifier, e.getMessage());
        }
        connection.close();
    }

        @Override
        public boolean supportsAggressiveRelease() {
            return false;
        }

        @Override
        public boolean isUnwrappableAs(Class<?> unwrapType) {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> unwrapType) {
            return null;
        }

        @Override
        public void customize(Map<String, Object> hibernateProperties) {
          hibernateProperties.put(MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER,this);
        }
}
