package com.kte.backend.config.tenantConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultiTenantConnectionProviderImplTest {

    @Mock private DataSource dataSource;
    @Mock private Connection connection;
    @Mock private Statement statement;

    @InjectMocks
    private MultiTenantConnectionProviderImpl provider;

    @BeforeEach
    void setUp() throws Exception {
        // lenient sur les deux : les tests release*/supportsAggressiveRelease n'appellent
        // ni dataSource.getConnection() ni connection.createStatement()
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(connection.createStatement()).thenReturn(statement);
    }

    // ---- getConnection : isolation du tenant ----

    @Test
    void getConnection_shouldSetSearchPath_forTenantSchema() throws Exception {
        provider.getConnection("tenant_acme");

        // Cœur de l'isolation : le schéma du tenant doit être placé en tête du search_path
        verify(statement).execute("SET search_path TO tenant_acme, public");
    }

    @Test
    void getConnection_shouldReturnConnection_fromDataSource() throws Exception {
        Connection result = provider.getConnection("tenant_acme");

        assertThat(result).isSameAs(connection);
    }

    @Test
    void getConnection_shouldNotSetSearchPath_whenTenantIsPublic() throws Exception {
        provider.getConnection("public");

        // "public" = schéma par défaut → aucun SET search_path
        verify(statement, never()).execute(anyString());
    }

    @Test
    void getConnection_shouldNotSetSearchPath_whenTenantIdentifierIsNull() throws Exception {
        provider.getConnection(null);

        // null → guard clause → aucun SET search_path
        verify(statement, never()).execute(anyString());
    }

    @Test
    void getConnection_shouldPropagateSQLException_whenSetSearchPathFails() throws Exception {
        when(statement.execute(anyString())).thenThrow(new SQLException("search_path error"));

        // L'exception doit remonter pour que l'appelant sache que la connexion est corrompue
        assertThatThrownBy(() -> provider.getConnection("tenant_acme"))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("search_path error");
    }

    // ---- releaseConnection : remise à zéro du search_path ----

    @Test
    void releaseConnection_shouldResetSearchPathToPublic() throws Exception {
        provider.releaseConnection("tenant_acme", connection);

        // CRITIQUE : sans ce reset, la prochaine requête du pool hérite du schéma tenant
        verify(statement).execute("SET search_path TO public");
    }

    @Test
    void releaseConnection_shouldCloseConnection() throws Exception {
        provider.releaseConnection("tenant_acme", connection);

        verify(connection).close();
    }

    @Test
    void releaseConnection_shouldStillCloseConnection_evenWhenResetFails() throws Exception {
        // Si le reset échoue, on ferme quand même la connexion pour éviter une fuite
        when(statement.execute("SET search_path TO public")).thenThrow(new SQLException("reset failed"));

        provider.releaseConnection("tenant_acme", connection);

        verify(connection).close();
    }

    // ---- getAnyConnection / releaseAnyConnection ----

    @Test
    void getAnyConnection_shouldReturnConnectionFromDataSource() throws Exception {
        assertThat(provider.getAnyConnection()).isSameAs(connection);
    }

    @Test
    void releaseAnyConnection_shouldCloseConnection() throws Exception {
        provider.releaseAnyConnection(connection);

        verify(connection).close();
    }

    // ---- config Hibernate ----

    @Test
    void supportsAggressiveRelease_shouldReturnFalse() {
        assertThat(provider.supportsAggressiveRelease()).isFalse();
    }
}