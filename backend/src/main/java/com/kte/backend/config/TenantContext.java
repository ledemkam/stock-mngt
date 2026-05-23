package com.kte.backend.config;

/**
 * TenantContext is a utility class that manages the current tenant information in a multi-tenant application.
 * It uses a ThreadLocal variable to store the tenant identifier for each thread,
 * allowing different threads to operate with different tenant contexts without interference.
 * This is particularly useful in scenarios where multiple tenants share the same application
 * instance but require data isolation and context-specific behavior.
 *
 * Flux:
 * 1.TenantFilter intercepts incoming requests and extracts the tenant identifier from the request (e.g., from headers or URL).
 * 2.TenantFilter sets the tenant identifier in the TenantContext using setCurrentTenant().
 * 3.Services and repositories can access the current tenant identifier using getCurrentTenant() to perform tenant-specific operations,
 * such as querying the database for tenant-specific data.
 * 4.After the request is processed, TenantFilter can clear the tenant context using clear() to prevent any potential memory leaks or
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_SCHEMA = new ThreadLocal<>();

    /**
     *
     * Sets the current tenant identifier for the current thread. This method is typically called by a filter or interceptor
     * that processes incoming requests and extracts the tenant information from the request context (e.g., from headers or URL).
     */
    public static void setCurrentTenant(final String tenant) {
        CURRENT_TENANT.set(tenant);
    }

    public static void setCurrentSchema(final String schema) {
        CURRENT_SCHEMA.set(schema);
    }

    /**
     *
     * Retrieves the current tenant identifier for the current thread. This method can be called by services or repositories
     * to access the tenant context and perform tenant-specific operations, such as querying the database for tenant-specific data.
     */

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static String getCurrentSchema() {
        return CURRENT_SCHEMA.get();
    }

    /**
     * Clears the current tenant identifier for the current thread.
     * This method should be called after processing a request to prevent
     */

    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_SCHEMA.remove();
    }
}
