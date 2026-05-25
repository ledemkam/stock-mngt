package com.kte.backend.controllers;

import com.kte.backend.common.PageReponse;
import com.kte.backend.services.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;


    @PostMapping(path = "/approve/{tenant-id}")
    public ResponseEntity<Void> approveTenant(
            @PathVariable("tenant-id")
            final String tenantId
    ) {
        tenantService.approveTenant(tenantId);
        return ResponseEntity.ok().build();
    }


    @PatchMapping(path = "/activate/{tenant-id}")
    public ResponseEntity<Void> activateTenant(
            @PathVariable("tenant-id")
            final String tenantId
    ) {
        tenantService.activateTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(path = "/deactivate/{tenant-id}")
    public ResponseEntity<Void> deactivateTenant(
            @PathVariable("tenant-id")
            final String tenantId
    ) {
        tenantService.deactivateTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(path = "/suspend/{tenant-id}")
    public ResponseEntity<Void> suspendTenant(
            @PathVariable("tenant-id")
            final String tenantId
    ) {
        tenantService.suspendTenant(tenantId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PageReponse> findAllTenants(
            @RequestParam(name = "page", defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            final int size
    ) {
        return ResponseEntity.ok(tenantService.findAll(page, size));
    }



}


