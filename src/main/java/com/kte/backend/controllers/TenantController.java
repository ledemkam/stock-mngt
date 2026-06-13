package com.kte.backend.controllers;

import com.kte.backend.common.PageReponse;
import com.kte.backend.controllers.uicontrollers.UITenantController;
import com.kte.backend.dto.responses.TenantResponse;
import com.kte.backend.services.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/tenants")
@Slf4j
public class TenantController implements UITenantController {

    private final TenantService tenantService;


    @Override
    @PostMapping(path = "/approve/{tenant-id}")
    public ResponseEntity<Void> approveTenant(
            @PathVariable("tenant-id")
            final String tenantId
    ) {
        tenantService.approveTenant(tenantId);
        log.info("Tenant with id {} approved successfully", tenantId);
        return ResponseEntity.ok().build();
    }


    @Override
    @PatchMapping(path = "/activate/{tenant-id}")
    public ResponseEntity<Void> activateTenant(
            @PathVariable("tenant-id")
            final String tenantId
    ) {
        tenantService.activateTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PatchMapping(path = "/deactivate/{tenant-id}")
    public ResponseEntity<Void> deactivateTenant(
            @PathVariable("tenant-id")
            final String tenantId
    ) {
        tenantService.deactivateTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @Override
    @PatchMapping(path = "/suspend/{tenant-id}")
    public ResponseEntity<Void> suspendTenant(
            @PathVariable("tenant-id")
            final String tenantId
    ) {
        tenantService.suspendTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping
    public ResponseEntity<PageReponse<TenantResponse>> findAllTenants(
            @RequestParam(name = "page", defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            final int size
    ) {
        return ResponseEntity.ok(tenantService.findAll(page, size));
    }



}


