package com.kte.backend.dto.responses;

import com.kte.backend.enums.TenantStatus;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private String tenantId;

    private String compagnyName;

    private String compagnyCode;

    private String email;

    private String adminFullName;

    private String adminEmail;

    private String adminUserName;

    private String adminPassword;

    private TenantStatus status;
}
