package com.kte.backend.services.tenant;

import com.kte.backend.common.PageReponse;
import com.kte.backend.dto.requests.RegisterTenantRequest;
import com.kte.backend.dto.responses.TenantResponse;

public interface TenantService {

    void registerTenant(final RegisterTenantRequest request);

    void approveTenant(final String tenantId);

    void activateTenant(final String tenantId);

     void deactivateTenant(final String tenantId);

     void suspendTenant(final String tenantId);

     PageReponse<TenantResponse> findAll(final int page, final int size);
}
