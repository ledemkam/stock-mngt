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
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceImplTest {

    @Mock private TenantRepository tenantRepository;
    @Mock private TenantMapper tenantMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserService userService;
    @Mock private ProvisioningService provisioningService;

    @InjectMocks
    private TenantServiceImpl tenantService;

    private RegisterTenantRequest request;
    private Tenant tenant;
    private TenantResponse response;

    @BeforeEach
    void setUp() {
        request = RegisterTenantRequest.builder()
                .compagnyName("Acme Corp")
                .compagnyCode("ACME")
                .email("acme@example.com")
                .adminFullName("Admin User")
                .adminEmail("admin@acme.com")
                .adminUserName("admin.acme")
                .adminPassword("secret123")
                .build();

        tenant = new Tenant();
        tenant.setId("tenant-001");
        tenant.setCompagnyName("Acme Corp");
        tenant.setCompagnyCode("ACME");
        tenant.setEmail("acme@example.com");
        tenant.setStatus(TenantStatus.PENDING);

        response = TenantResponse.builder()
                .tenantId("tenant-001")
                .compagnyName("Acme Corp")
                .status(TenantStatus.PENDING)
                .build();
    }

    // ---- registerTenant ----

    @Test
    void registerTenant_shouldSaveTenant_whenCodeAndEmailAreUnique() {
        when(tenantRepository.existsByCompagnyCode(request.getCompagnyCode())).thenReturn(false);
        when(tenantRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(tenantMapper.toEntity(request)).thenReturn(tenant);
        when(passwordEncoder.encode(request.getAdminPassword())).thenReturn("encodedSecret");

        tenantService.registerTenant(request);

        verify(tenantRepository).save(tenant);
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.PENDING);
        assertThat(tenant.getAdminPassword()).isEqualTo("encodedSecret");
    }

    @Test
    void registerTenant_shouldThrowDuplicateEntityException_whenCompagnyCodeAlreadyExists() {
        when(tenantRepository.existsByCompagnyCode(request.getCompagnyCode())).thenReturn(true);

        assertThatThrownBy(() -> tenantService.registerTenant(request))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Compagny code already exists");

        verify(tenantRepository, never()).save(any());
    }

    @Test
    void registerTenant_shouldThrowDuplicateEntityException_whenEmailAlreadyExists() {
        when(tenantRepository.existsByCompagnyCode(request.getCompagnyCode())).thenReturn(false);
        when(tenantRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> tenantService.registerTenant(request))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Email already exists");

        verify(tenantRepository, never()).save(any());
    }

    // ---- approveTenant ----

    @Test
    void approveTenant_shouldSetActiveAndProvisionAndCreateAdmin_whenTenantExists() {
        when(tenantRepository.findById("tenant-001")).thenReturn(Optional.of(tenant));

        tenantService.approveTenant("tenant-001");

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        verify(tenantRepository, atLeastOnce()).save(tenant);
        verify(provisioningService).provisionTenant(tenant);
        verify(userService).createAdminUser(tenant);
    }

    @Test
    void approveTenant_shouldThrowEntityNotFoundException_whenTenantNotFound() {
        when(tenantRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.approveTenant("unknown"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tenant not found with id: unknown");

        verify(provisioningService, never()).provisionTenant(any());
    }

    @Test
    void approveTenant_shouldRollbackStatusToPending_whenProvisioningFails() {
        when(tenantRepository.findById("tenant-001")).thenReturn(Optional.of(tenant));
        doThrow(new RuntimeException("Provisioning error"))
                .when(provisioningService).provisionTenant(any());

        assertThatThrownBy(() -> tenantService.approveTenant("tenant-001"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to approve tenant");

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.PENDING);
        verify(userService, never()).createAdminUser(any());
    }

    @Test
    void approveTenant_shouldRollbackStatusToPending_whenAdminCreationFails() {
        when(tenantRepository.findById("tenant-001")).thenReturn(Optional.of(tenant));
        doThrow(new RuntimeException("Admin creation error"))
                .when(userService).createAdminUser(any());

        assertThatThrownBy(() -> tenantService.approveTenant("tenant-001"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to approve tenant");

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.PENDING);
    }

    // ---- activateTenant ----

    @Test
    void activateTenant_shouldSaveTenant_whenTenantIsPending() {
        tenant.setStatus(TenantStatus.PENDING);
        when(tenantRepository.findById("tenant-001")).thenReturn(Optional.of(tenant));

        tenantService.activateTenant("tenant-001");

        verify(tenantRepository).save(tenant);
    }

    @Test
    void activateTenant_shouldThrowEntityNotFoundException_whenTenantNotFound() {
        when(tenantRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.activateTenant("unknown"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    void activateTenant_shouldThrowInvalidRequestException_whenTenantIsNotPending() {
        tenant.setStatus(TenantStatus.ACTIVE);
        when(tenantRepository.findById("tenant-001")).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> tenantService.activateTenant("tenant-001"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Only pending tenants can be activated");

        verify(tenantRepository, never()).save(any());
    }

    // ---- deactivateTenant ----

    @Test
    void deactivateTenant_shouldSetInactive_whenTenantIsActive() {
        tenant.setStatus(TenantStatus.ACTIVE);
        when(tenantRepository.findById("tenant-001")).thenReturn(Optional.of(tenant));

        tenantService.deactivateTenant("tenant-001");

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.INACTIVE);
        verify(tenantRepository).save(tenant);
    }

    @Test
    void deactivateTenant_shouldThrowEntityNotFoundException_whenTenantNotFound() {
        when(tenantRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.deactivateTenant("unknown"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    void deactivateTenant_shouldThrowInvalidRequestException_whenTenantIsNotActive() {
        tenant.setStatus(TenantStatus.PENDING);
        when(tenantRepository.findById("tenant-001")).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> tenantService.deactivateTenant("tenant-001"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Only active tenants can be deactivated");

        verify(tenantRepository, never()).save(any());
    }

    // ---- suspendTenant ----

    @Test
    void suspendTenant_shouldSetSuspended_whenTenantIsActive() {
        tenant.setStatus(TenantStatus.ACTIVE);
        when(tenantRepository.findById("tenant-001")).thenReturn(Optional.of(tenant));

        tenantService.suspendTenant("tenant-001");

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
        verify(tenantRepository).save(tenant);
    }

    @Test
    void suspendTenant_shouldThrowEntityNotFoundException_whenTenantNotFound() {
        when(tenantRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tenantService.suspendTenant("unknown"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tenant not found");
    }

    @Test
    void suspendTenant_shouldThrowInvalidRequestException_whenTenantIsNotActive() {
        tenant.setStatus(TenantStatus.PENDING);
        when(tenantRepository.findById("tenant-001")).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> tenantService.suspendTenant("tenant-001"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Only active tenants can be suspended");

        verify(tenantRepository, never()).save(any());
    }

    // ---- findAll ----

    @Test
    void findAll_shouldReturnPagedResponse() {
        Page<Tenant> tenantPage = new PageImpl<>(List.of(tenant), PageRequest.of(0, 10), 1);
        when(tenantRepository.findAll(any(PageRequest.class))).thenReturn(tenantPage);
        when(tenantMapper.toResponse(tenant)).thenReturn(response);

        PageReponse<TenantResponse> result = tenantService.findAll(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}