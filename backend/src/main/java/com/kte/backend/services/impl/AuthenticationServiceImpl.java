package com.kte.backend.services.impl;

import com.kte.backend.dto.requests.LoginRequest;
import com.kte.backend.dto.responses.LoginReponse;
import com.kte.backend.entities.User;
import com.kte.backend.security.JwtTokenService;
import com.kte.backend.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    @Override
    public LoginReponse login(LoginRequest request) {
        final Authentication  authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        final User user = (User) authentication.getPrincipal();

        final String token = jwtTokenService.generateAccessToken(
                user.getTenantId(),
                user.getId(),
                user.getRole().name());

        final String tokenType = "Bearer";

        return LoginReponse.builder()
                .accessToken(token)
                .tokenType(tokenType)
                .build();
    }
}
