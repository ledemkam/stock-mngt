package com.kte.backend.services.tenant.impl;

import com.kte.backend.common.PageReponse;
import com.kte.backend.dto.requests.RegisterTenantRequest;
import com.kte.backend.dto.responses.TenantResponse;
import com.kte.backend.entities.Tenant;
import com.kte.backend.enums.TenantStatus;
import com.kte.backend.exceptions.DuplicateEntityException;
import com.kte.backend.mappers.TenantMapper;
import com.kte.backend.repositories.TenantRepository;
import com.kte.backend.services.tenant.TenantService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    public void registerTenant(RegisterTenantRequest request) {
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
    public void approveTenant(String tenantId) {

    }

    @Override
    public void activateTenant(String tenantId) {

    }

    @Override
    public void deactivateTenant(String tenantId) {

    }

    @Override
    public void suspendTenant(String tenantId) {

    }

    @Override
    public PageReponse<TenantResponse> findAll(int page, int size) {
        return null;
    }
}
