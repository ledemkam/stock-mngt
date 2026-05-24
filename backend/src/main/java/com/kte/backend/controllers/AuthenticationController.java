package com.kte.backend.controllers;


import com.kte.backend.dto.requests.LoginRequest;
import com.kte.backend.dto.responses.LoginReponse;
import com.kte.backend.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginReponse> login (
            @Valid
            @RequestBody
            final LoginRequest request
            ){
        final LoginReponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

 }
