package com.kte.backend.services.tenant;

import com.kte.backend.entities.Tenant;

public interface ProvisioningService {
    void provisionTenant(final Tenant tenant);
}
