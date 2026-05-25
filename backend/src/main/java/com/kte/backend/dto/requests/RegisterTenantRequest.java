package com.kte.backend.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RegisterTenantRequest {

    @NotBlank(message = "Company name is required")
    private String compagnyName;

    @NotBlank(message = "Company code is required")
    private String compagnyCode;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Admin full name is required")
    private String adminFullName;

    @NotBlank(message = "Admin email is required")
    private String adminEmail;

    @NotBlank(message = "Admin username is required")
    private  String adminUserName;

    @NotBlank(message = "Admin password is required")
    private String adminPassword;
}
