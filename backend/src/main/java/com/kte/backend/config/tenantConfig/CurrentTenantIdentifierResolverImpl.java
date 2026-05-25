package com.kte.backend.config.tenantConfig;


import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class CurrentTenantIdentifierResolverImpl  implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {


    @Override
    public String resolveCurrentTenantIdentifier() {
        final String schema = TenantContext.getCurrentSchema();
        log.trace("Resolving current tenant identifier, current schema: {}", schema);
        return schema;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
       hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,this);
    }
}
