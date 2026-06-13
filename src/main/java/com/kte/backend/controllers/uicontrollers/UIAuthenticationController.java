package com.kte.backend.controllers.uicontrollers;

import com.kte.backend.dto.ErrorDto;
import com.kte.backend.dto.requests.LoginRequest;
import com.kte.backend.dto.requests.RegisterTenantRequest;
import com.kte.backend.dto.responses.LoginReponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "UI Authentication Controllers", description = "Endpoints for authentication and tenant registration")
public interface UIAuthenticationController {

    @Operation(summary = "Login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
    })
    ResponseEntity<LoginReponse> login(@Valid @RequestBody final LoginRequest request);

    @Operation(summary = "Register Tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - Tenant already exists",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
    })
    ResponseEntity<Void> register(@Valid @RequestBody final RegisterTenantRequest request);
}