package com.kte.backend.config.tenantConfig;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSchemaResolver {

    private final JdbcTemplate jdbcTemplate;

    private static final String PUBLIC_SCHEMA = "public";

    @Cacheable(value = "tenantSchemas",key = "#tenantId")
    public String resolveTenantSchema(final String tenantId){
        if (tenantId == null){
            return PUBLIC_SCHEMA;
        }
        try{
            final String compagnyCode = jdbcTemplate
                    .queryForObject("SELECT compagny_code FROM public.tenants WHERE id = ? AND deleted= false",
                            String.class,
                            tenantId);
            if (compagnyCode != null){
                final String schemaName = "tenant_ " + compagnyCode.toLowerCase();
                log.debug("Resolved tenant schema : {} for tenantId: {}", tenantId, compagnyCode);
                return schemaName;
            }
            log.warn("Tenant with id {} not found, using public schema", tenantId);
            return PUBLIC_SCHEMA;
        }catch (final Exception e){
              log.error("Error resolving tenant schema for tenantId {}: {}", tenantId, e.getMessage());
              return PUBLIC_SCHEMA;
        }
    }
}
