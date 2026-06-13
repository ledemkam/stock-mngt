package com.kte.backend.controllers;


import com.kte.backend.controllers.uicontrollers.UIAuthenticationController;
import com.kte.backend.dto.requests.LoginRequest;
import com.kte.backend.dto.requests.RegisterTenantRequest;
import com.kte.backend.dto.responses.LoginReponse;
import com.kte.backend.services.auth.AuthenticationService;
import com.kte.backend.services.tenant.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/auth")
public class AuthenticationController implements UIAuthenticationController {

    private final AuthenticationService authenticationService;
    private final TenantService tenantService;

    @Override
    @PostMapping(path = "/login")
    public ResponseEntity<LoginReponse> login (
            @Valid
            @RequestBody
            final LoginRequest request
            ){
        final LoginReponse response = authenticationService.login(request);
        log.info("User {} logged in successfully for tenant {}", request.getUsername(),response.getAccessToken());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping(path = "/register")
    public ResponseEntity<Void> register(
            @Valid
            @RequestBody
            final RegisterTenantRequest request
    ){
        tenantService.registerTenant(request);
        log.info("Tenant registered successfully with name: {}", request.getCompagnyName());
        return ResponseEntity.ok().build();
    }

 }
