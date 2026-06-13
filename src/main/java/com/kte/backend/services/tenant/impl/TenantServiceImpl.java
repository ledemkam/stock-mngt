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
import com.kte.backend.services.auth.UserService;
import com.kte.backend.services.tenant.ProvisioningService;
import com.kte.backend.services.tenant.TenantService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final ProvisioningService provisioningService;

    @Override
    @Transactional
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
     log.info("Registering new tenant: {}", tenant);
     tenantRepository.save(tenant);

    }

    @Override
    public void approveTenant(final String tenantId) {
        //checking
        final Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + tenantId));

        //activate tenant
        tenant.setStatus(TenantStatus.ACTIVE);
        log.info("Approving tenant with id: {}", tenantId);
        tenantRepository.save(tenant);

        //provision the schema for the tenant
            //we use trycatch here,because rollback
        try {
        provisioningService.provisionTenant(tenant);
        //create initial admin user
        userService.createAdminUser(tenant);
        log.info("Tenant with id {} approved and initial admin user created", tenantId);
            } catch (final Exception e) {
                log.error("Error approving tenant with id {}: {}", tenantId, e.getMessage());
                //rollback tenant status to pending
                rollbackTenantStatus(tenant);
                throw new RuntimeException("Failed to approve tenant with id " + tenantId + ": " + e.getMessage());
        }

    }



    @Override
    public void activateTenant(final String tenantId) {
        final Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with id: " + tenantId));

        if (tenant.getStatus() != TenantStatus.PENDING) {
            throw new InvalidRequestException("Only pending tenants can be activated");
        }
        log.info("Activating tenant with id: {}", tenantId);
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

        log.info("Deactivating tenant with id: {}", tenantId);
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

        log.info("Suspending tenant with id: {}", tenantId);
        tenantRepository.save(tenant);

    }

    @Override
    public PageReponse<TenantResponse> findAll(int page, int size) {
        log.debug("Fetching tenants - page: {}, size: {}", page, size);
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Tenant> tenants = tenantRepository.findAll(pageRequest);
        final Page<TenantResponse> tenantResponses= tenants.map(tenantMapper::toResponse);
        log.debug("Fetched {} tenants", tenants.getNumberOfElements());
        return PageReponse.of(tenantResponses);
    }

    private void rollbackTenantStatus(final Tenant tenant){
        tenant.setStatus(TenantStatus.PENDING);
        tenantRepository.save(tenant);
    }


}
