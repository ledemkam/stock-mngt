package com.kte.backend.config;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * TenantHibernateFilter is an aspect that intercepts repository or service
 * method executions to activate the tenant
 * filter in Hibernate.
 * It retrieves the current tenant identifier from the TenantContext and enables the tenant filter
 * for the current session,
 * ensuring that all database operations performed within the scope of the repository methods
 * are filtered based on the tenant context.
 *
 * This aspect is crucial for maintaining data isolation in a multi-tenant application, as it ensures that each tenant's
 * data is accessed and manipulated
 * according to the tenant context set by the TenantFilter.
 *
 * @Aspect actived automatic tenant filter
 */

@Aspect
@Component
public class TenantHibernateFilter {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.kte.backend.services.*.*(..))")
    private void activateTenantFilter(){
        final String tenantId = TenantContext.getCurrentTenant();

        if (tenantId != null) {
            final Session session = entityManager.unwrap(Session.class);

            // activate tenant filter and set the tenantId parameter to the current tenant identifier,
            // ensuring that all database operations

             session.enableFilter("tenantFilter")
                     .setParameter("tenantId", tenantId);
        }
    }

}
