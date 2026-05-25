package com.kte.backend.services.tenant.impl;

import com.kte.backend.common.PageReponse;
import com.kte.backend.dto.requests.RegisterTenantRequest;
import com.kte.backend.dto.responses.TenantResponse;
import com.kte.backend.entities.Tenant;
import com.kte.backend.enums.TenantStatus;
import com.kte.backend.exceptions.DuplicateEntityException;
import com.kte.backend.exceptions.InvalidRequestException;
import com.kte.backend.mappers.TenantMapper;
import com.kte.backend.repositories.TenantRepository;
import com.kte.backend.services.tenant.TenantService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Transactional
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void registerTenant(final RegisterTenantRequest request) {
        // check if tenant already exist
      if(tenantRepository.existsByCompagnyCode(request.getCompagnyCode())){
          throw new DuplicateEntityException("Compagny code already exists");
      }

     if (tenantRepository.existsByEmail(request.getEmail())){
         throw new DuplicateEntityException("Email already exists");
     }

     // create tenant entity
     final Tenant tenant = this.tenantMapper.toEntity(request);
     tenant.setAdminPassword(passwordEncoder.encode(request.getAdminPassword()));
     tenant.setStatus(TenantStatus.PENDING);

     tenantRepository.save(tenant);

    }

    @Override
    public void approveTenant(final String tenantId) {

    }

    @Override
    public void activateTenant(final String tenantId) {
        final Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + tenantId));

        if (tenant.getStatus() != TenantStatus.PENDING) {
            throw new InvalidRequestException("Only pending tenants can be activated");
        }

        tenantRepository.save(tenant);

    }

    @Override
    public void deactivateTenant(String tenantId) {
        final Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + tenantId));

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new InvalidRequestException("Only active tenants can be deactivated");
        }

        tenant.setStatus(TenantStatus.INACTIVE);
        tenantRepository.save(tenant);

    }

    @Override
    public void suspendTenant(String tenantId) {
        final Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + tenantId));

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new InvalidRequestException("Only active tenants can be suspended");
        }

        tenant.setStatus(TenantStatus.SUSPENDED);
        tenantRepository.save(tenant);

    }

    @Override
    public PageReponse<TenantResponse> findAll(int page, int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Tenant> tenants = tenantRepository.findAll(pageRequest);
        final Page<TenantResponse> tenantResponses= tenants.map(tenantMapper::toResponse);
        return PageReponse.of(tenantResponses);
    }
}
