package com.kte.backend.services.auth.impl;

import com.kte.backend.dto.requests.LoginRequest;
import com.kte.backend.dto.responses.LoginReponse;
import com.kte.backend.entities.Tenant;
import com.kte.backend.entities.User;
import com.kte.backend.enums.UserRole;
import com.kte.backend.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private LoginRequest loginRequest;
    private User user;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("john.doe", "secret123");

        Tenant tenant = new Tenant();
        tenant.setId("tenant-001");

        user = User.builder()
                .username("john.doe")
                .email("john@example.com")
                .password("encodedSecret")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.ROLE_USER)
                .enable(true)
                .tenant(tenant)
                .build();
        user.setId("user-001");

        authentication = mock(Authentication.class);
    }

    @Test
    void login_shouldReturnLoginResponse_whenCredentialsAreValid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtTokenService.generateAccessToken("tenant-001", "user-001", UserRole.ROLE_USER.name()))
                .thenReturn("mocked.jwt.token");

        LoginReponse result = authenticationService.login(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("mocked.jwt.token");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_shouldPassCorrectCredentialsToAuthenticationManager() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtTokenService.generateAccessToken(any(), any(), any())).thenReturn("token");

        authenticationService.login(loginRequest);

        verify(authenticationManager).authenticate(
                argThat(token ->
                        token instanceof UsernamePasswordAuthenticationToken t &&
                        "john.doe".equals(t.getPrincipal()) &&
                        "secret123".equals(t.getCredentials())
                )
        );
    }

    @Test
    void login_shouldCallJwtServiceWithCorrectClaims() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtTokenService.generateAccessToken("tenant-001", "user-001", UserRole.ROLE_USER.name()))
                .thenReturn("token");

        authenticationService.login(loginRequest);

        verify(jwtTokenService).generateAccessToken("tenant-001", "user-001", UserRole.ROLE_USER.name());
    }

    @Test
    void login_shouldThrowException_whenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");

        verify(jwtTokenService, never()).generateAccessToken(any(), any(), any());
    }
}