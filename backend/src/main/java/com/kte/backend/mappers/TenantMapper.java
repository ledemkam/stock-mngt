package com.kte.backend.mappers;

import com.kte.backend.dto.requests.RegisterTenantRequest;
import com.kte.backend.dto.responses.TenantResponse;
import com.kte.backend.entities.Tenant;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TenantMapper {

    public Tenant toEntity(final RegisterTenantRequest request){
        return Tenant.builder()
                .compagnyName(request.getCompagnyName())
                .compagnyCode(request.getCompagnyCode())
                .createdAt(LocalDateTime.now())
                .email(request.getEmail())
                .adminFullName(request.getAdminFullName())
                .adminUserName(request.getAdminUserName())
                //.adminPassword(request.getAdminPassword()) ->here can ignored,direct implement in service
                .adminEmail(request.getAdminEmail())
                .build();
    }

    public TenantResponse toResponse(final Tenant entity){
        return TenantResponse.builder()
                .compagnyName(entity.getCompagnyName())
                .compagnyCode(entity.getCompagnyCode())
                .createdAt(entity.getCreatedAt())
                .email(entity.getEmail())
                .adminFullName(entity.getAdminFullName())
                .adminUserName(entity.getAdminUserName())
                .adminEmail(entity.getAdminEmail())
                .status(entity.getStatus())
                .build();
    }
}
