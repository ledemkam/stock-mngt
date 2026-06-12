package com.kte.backend.security;

import com.kte.backend.exceptions.UnauthorizedException;
import com.kte.backend.properties.JwtProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtTokenService jwtTokenService;

    private static PrivateKey testPrivateKey;
    private static PublicKey testPublicKey;

    private static final String TENANT_ID = "tenant-001";
    private static final String USER_ID   = "user-001";
    private static final String ROLE      = "ROLE_USER";
    private static final long   ONE_HOUR  = 3_600_000L;

    @BeforeAll
    static void generateRsaKeyPair() throws Exception {
        // Paire RSA 2048 bits générée en mémoire — aucun fichier PEM nécessaire
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        testPrivateKey = pair.getPrivate();
        testPublicKey  = pair.getPublic();
    }

    @BeforeEach
    void setUp() {
        // Contourne @PostConstruct : injecte les clés directement dans le service
        ReflectionTestUtils.setField(jwtTokenService, "privateKey", testPrivateKey);
        ReflectionTestUtils.setField(jwtTokenService, "publicKey",  testPublicKey);

        // Stub par défaut (lenient = pas d'UnnecessaryStubbingException si surchargé)
        lenient().when(jwtProperties.getAccessTokenExpiration()).thenReturn(ONE_HOUR);
    }

    // ---- generateAccessToken ----

    @Test
    void generateAccessToken_shouldReturnNonBlankToken() {
        String token = jwtTokenService.generateAccessToken(TENANT_ID, USER_ID, ROLE);

        assertThat(token).isNotBlank();
    }

    @Test
    void generateAccessToken_shouldProduceThreePartJwt() {
        String token = jwtTokenService.generateAccessToken(TENANT_ID, USER_ID, ROLE);

        // format JWT : header.payload.signature
        assertThat(token.split("\\.")).hasSize(3);
    }

    // ---- validateToken ----

    @Test
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        String token = jwtTokenService.generateAccessToken(TENANT_ID, USER_ID, ROLE);

        assertThat(jwtTokenService.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldThrowUnauthorizedException_whenTokenIsExpired() {
        // Expiration dans le passé → token immédiatement expiré
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(-1_000L);
        String expiredToken = jwtTokenService.generateAccessToken(TENANT_ID, USER_ID, ROLE);

        assertThatThrownBy(() -> jwtTokenService.validateToken(expiredToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void validateToken_shouldThrowUnauthorizedException_whenTokenIsMalformed() {
        assertThatThrownBy(() -> jwtTokenService.validateToken("not.a.valid.jwt"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void validateToken_shouldThrowUnauthorizedException_whenTokenIsEmpty() {
        assertThatThrownBy(() -> jwtTokenService.validateToken(""))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void validateToken_shouldThrowUnauthorizedException_whenSignatureIsInvalid() throws Exception {
        // Token signé avec une autre clé privée → signature invalide
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        PrivateKey otherKey = gen.generateKeyPair().getPrivate();
        ReflectionTestUtils.setField(jwtTokenService, "privateKey", otherKey);

        String tokenWithOtherKey = jwtTokenService.generateAccessToken(TENANT_ID, USER_ID, ROLE);

        // Restaure la clé originale pour la vérification
        ReflectionTestUtils.setField(jwtTokenService, "privateKey", testPrivateKey);

        assertThatThrownBy(() -> jwtTokenService.validateToken(tokenWithOtherKey))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ---- extraction des claims ----

    @Test
    void getUserIdFromToken_shouldReturnUserId() {
        String token = jwtTokenService.generateAccessToken(TENANT_ID, USER_ID, ROLE);

        assertThat(jwtTokenService.getUserIdFromTokEN(token)).isEqualTo(USER_ID);
    }

    @Test
    void getTenantIdFromToken_shouldReturnTenantId() {
        String token = jwtTokenService.generateAccessToken(TENANT_ID, USER_ID, ROLE);

        assertThat(jwtTokenService.getTenantIdFromToken(token)).isEqualTo(TENANT_ID);
    }

    @Test
    void getRoleFromToken_shouldReturnRole() {
        String token = jwtTokenService.generateAccessToken(TENANT_ID, USER_ID, ROLE);

        assertThat(jwtTokenService.getRoleFromToken(token)).isEqualTo(ROLE);
    }

    @Test
    void generateAccessToken_claimsShouldMatchInputExactly() {
        String token = jwtTokenService.generateAccessToken(TENANT_ID, USER_ID, ROLE);

        assertThat(jwtTokenService.getUserIdFromTokEN(token)).isEqualTo(USER_ID);
        assertThat(jwtTokenService.getTenantIdFromToken(token)).isEqualTo(TENANT_ID);
        assertThat(jwtTokenService.getRoleFromToken(token)).isEqualTo(ROLE);
    }
}