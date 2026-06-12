package com.kte.backend.security;

import com.kte.backend.config.tenantConfig.TenantContext;
import com.kte.backend.config.tenantConfig.TenantSchemaResolver;
import com.kte.backend.exceptions.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthentificationFilterTest {

    @Mock private JwtTokenService jwtTokenService;
    @Mock private TenantSchemaResolver tenantSchemaResolver;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthentificationFilter filter;

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    // ---- endpoints publics : filtre ignoré ----

    @Test
    void doFilterInternal_shouldSkipFilter_forLoginEndpoint() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void doFilterInternal_shouldSkipFilter_forRegisterEndpoint() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/register");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenService);
    }

    // ---- token absent ----

    @Test
    void doFilterInternal_shouldNotSetAuthentication_whenAuthorizationHeaderIsMissing() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/products");
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void doFilterInternal_shouldNotSetAuthentication_whenHeaderHasNoBearerPrefix() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/products");
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenService);
    }

    // ---- token valide ----

    @Test
    void doFilterInternal_shouldSetAuthentication_whenTokenIsValid() throws Exception {
        stubValidToken("tenant-001", "user-001", "ROLE_USER");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("user-001");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldSetCorrectAuthority_whenTokenIsValid() throws Exception {
        stubValidToken("tenant-001", "user-001", "ROLE_COMPAGNY_ADMIN");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_COMPAGNY_ADMIN");
    }

    @Test
    void doFilterInternal_shouldPopulateTenantContext_duringChainExecution() throws Exception {
        stubValidToken("tenant-001", "user-001", "ROLE_USER");

        // capture l'état du TenantContext pendant l'exécution de la chaîne
        doAnswer(inv -> {
            assertThat(TenantContext.getCurrentTenant()).isEqualTo("tenant-001");
            assertThat(TenantContext.getCurrentSchema()).isEqualTo("tenant_acme");
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        verify(tenantSchemaResolver).resolveTenantSchema("tenant-001");
    }

    @Test
    void doFilterInternal_shouldClearTenantContext_afterFilterChain() throws Exception {
        stubValidToken("tenant-001", "user-001", "ROLE_USER");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(TenantContext.getCurrentTenant()).isNull();
        assertThat(TenantContext.getCurrentSchema()).isNull();
    }

    @Test
    void doFilterInternal_shouldNotSetTenantContext_whenTenantIdIsNull() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/products");
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtTokenService.validateToken("token")).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN("token")).thenReturn("user-001");
        when(jwtTokenService.getTenantIdFromToken("token")).thenReturn(null);
        when(jwtTokenService.getRoleFromToken("token")).thenReturn("ROLE_USER");

        filter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(tenantSchemaResolver);
        assertThat(TenantContext.getCurrentTenant()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // ---- token invalide ----

    @Test
    void doFilterInternal_shouldReturn401_whenTokenValidationThrows() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/products");
        when(request.getHeader("Authorization")).thenReturn("Bearer badToken");
        when(jwtTokenService.validateToken("badToken"))
                .thenThrow(new UnauthorizedException("JWT invalid"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_shouldReturn401_whenTokenIsExpired() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/products");
        when(request.getHeader("Authorization")).thenReturn("Bearer expiredToken");
        when(jwtTokenService.validateToken("expiredToken"))
                .thenThrow(new UnauthorizedException("JWT token is expired"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_shouldNotSetAuthentication_whenTokenValidationFails() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/products");
        when(request.getHeader("Authorization")).thenReturn("Bearer badToken");
        when(jwtTokenService.validateToken("badToken"))
                .thenThrow(new UnauthorizedException("JWT invalid"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ---- helper ----

    private void stubValidToken(String tenantId, String userId, String role) throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/products");
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtTokenService.validateToken("validToken")).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN("validToken")).thenReturn(userId);
        when(jwtTokenService.getTenantIdFromToken("validToken")).thenReturn(tenantId);
        when(jwtTokenService.getRoleFromToken("validToken")).thenReturn(role);
        when(tenantSchemaResolver.resolveTenantSchema(tenantId)).thenReturn("tenant_acme");
    }
}