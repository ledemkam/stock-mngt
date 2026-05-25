package com.kte.backend.controllers;


import com.kte.backend.dto.requests.LoginRequest;
import com.kte.backend.dto.requests.RegisterTenantRequest;
import com.kte.backend.dto.responses.LoginReponse;
import com.kte.backend.services.auth.AuthenticationService;
import com.kte.backend.services.tenant.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final TenantService tenantService;

    @PostMapping(path = "/login")
    public ResponseEntity<LoginReponse> login (
            @Valid
            @RequestBody
            final LoginRequest request
            ){
        final LoginReponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

   @PostMapping(path = "/register")
    public ResponseEntity<Void> register(
            @Valid
            @RequestBody
            final RegisterTenantRequest request
    ){
        tenantService.registerTenant(request);
        return ResponseEntity.ok().build();
    }

 }
