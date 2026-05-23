package com.kte.backend.security;

import com.kte.backend.exceptions.UnauthorizedException;
import com.kte.backend.properties.JwtProperties;
import io.jsonwebtoken.*;

import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.InputStream;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private PrivateKey privateKey;
    private PublicKey publicKey;


    @PostConstruct
    public void init(){
        try {
            this.privateKey = loadPrivateKey(jwtProperties.getPrivateKeyPath());
            this.publicKey = loadPublicKey(jwtProperties.getPublicKeyPath());
            log.info("Private and Public keys loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load on of keys: {}", e.getMessage());
            throw new RuntimeException("Failed to load on of keys", e);
        }
    }

    public  String generateAccessToken(
            @NotNull
            final String tenantId,
            @NotNull
            final String userId,
            final String role
    ){
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration().toMillis());
        return Jwts.builder()
                .subject(userId)
                .claim("tenantId", tenantId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(privateKey,Jwts.SIG.RS256)
                .compact();
    }

    public String getUserIdFromTokEN(final String token){
        final Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public String getTenantIdFromToken(final String token){
        final Claims claims = getClaimsFromToken(token);
        return claims.get("tenantId", String.class);
    }

    public String getRoleFromToken(final String token){
        final Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public boolean validateToken(final String token){
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (final ExpiredJwtException e) {

            log.warn("JWT token is expired: {}", e.getMessage());
            throw new UnauthorizedException("JWT token is expired", e);
        } catch (final UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw new UnauthorizedException("JWT token is unsupported", e);
        } catch (final MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw new UnauthorizedException("JWT token is malformed", e);
        } catch (final SecurityException e) {
            log.warn("JWT token  is invalid: {}", e.getMessage());
            throw new UnauthorizedException("JWT token  is invalid", e);
        } catch (final IllegalArgumentException e) {
            log.warn("JWT token is empty or null: {}", e.getMessage());
            throw new UnauthorizedException("JWT token is empty or null", e);
        }
    }

    private Claims getClaimsFromToken(final String token){
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PrivateKey loadPrivateKey(final String privateKeyPath) throws Exception {
        // Implement logic to load private key from the specified path
        // This could involve reading a PEM file and converting it to a PrivateKey object

        try (final InputStream is = JwtTokenService.class.getClassLoader().getResourceAsStream(privateKeyPath)) {
            if (is == null) {
                throw new RuntimeException("Private key file not found: " + privateKeyPath);
            }
            final String key = new String(is.readAllBytes());
            final String privateKeyPEM = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            final byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
            final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        }
    }

    private PublicKey loadPublicKey(final String publicKeyPath) throws Exception {
        try (final InputStream is = JwtTokenService.class.getClassLoader().getResourceAsStream(publicKeyPath)) {
            if (is == null) {
                throw new RuntimeException("Publickey file not found: " + publicKeyPath);
            }
            final String key = new String(is.readAllBytes());
            final String publicKeyPEM = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            final byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
            final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        }
    }
}
