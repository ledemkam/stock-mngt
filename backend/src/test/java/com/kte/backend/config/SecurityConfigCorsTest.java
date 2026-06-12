package com.kte.backend.config;

import com.kte.backend.security.JwtAuthentificationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigCorsTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(mock(JwtAuthentificationFilter.class));
    }

    private CorsConfiguration corsConfig() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        Map<String, CorsConfiguration> configs =
                ((UrlBasedCorsConfigurationSource) source).getCorsConfigurations();
        return configs.get("/**");
    }

    @Test
    void corsConfigurationSource_shouldRegisterConfigForAllPaths() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        Map<String, CorsConfiguration> configs =
                ((UrlBasedCorsConfigurationSource) source).getCorsConfigurations();

        assertThat(configs).containsKey("/**");
    }

    @Test
    void corsConfigurationSource_shouldAllowAllOriginPatterns() {
        assertThat(corsConfig().getAllowedOriginPatterns()).containsExactly("*");
    }

    @Test
    void corsConfigurationSource_shouldAllowAllStandardHttpMethods() {
        assertThat(corsConfig().getAllowedMethods())
                .containsExactlyInAnyOrder("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
    }

    @Test
    void corsConfigurationSource_shouldAllowAllHeaders() {
        assertThat(corsConfig().getAllowedHeaders()).containsExactly("*");
    }

    @Test
    void corsConfigurationSource_shouldAllowCredentials() {
        assertThat(corsConfig().getAllowCredentials()).isTrue();
    }

    @Test
    void corsConfigurationSource_shouldSetMaxAgeTo3600Seconds() {
        assertThat(corsConfig().getMaxAge()).isEqualTo(3600L);
    }
}