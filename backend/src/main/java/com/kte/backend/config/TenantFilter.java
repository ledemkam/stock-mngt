package com.kte.backend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Tenant - Intercep every http request for indentified tenant
 *
 * The TenantFilter is a servlet filter that intercepts incoming HTTP requests to identify the tenant making the request.
 * It extracts the tenant identifier from the request (e.g., from headers or URL)
 *
 * this filter is the entry point for mechanism multi tenant
 * his extract the tenant information from the request and set it in the TenantContext,
 * allowing services and repositories to access the tenant context and perform tenant-specific operations.
 *
 * indentiy strategy: the tenant identifier is expected to be provided in the request header "X-Tenant-ID".
 * If the tenant identifier is missing or invalid, the filter responds with a 400 Bad Request status and an error message
 * indicating that the tenant ID is required.
 *
 * (Optional) Sub-domain: alpha.stockapp.com -> "alpha"
 */

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this filter is executed before other filters (e.g., authentication filters)
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        final String tenantId = resolveTenant(request);
        if (tenantId == null || tenantId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Tenant ID is missing or invalid," +
                    "please add the header X-Tenant-ID\"}");
            return;
        }

        try {
            //save the tenant information in the TenantContext for the current thread, allowing services and repositories
            // to access the tenant context and perform tenant-specific operations, such as querying the database for
            // tenant-specific data.

            TenantContext.setCurrentTenant(tenantId);

            //continue with the filter chain -> controller -> service,
            // allowing the request to be processed by
            // the next filter or servlet in the chain.
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            //CRITIK: always clear the ThreadLocal after request processing to prevent memory
            // leaks and ensure that the tenant context does not persist across requests.
            TenantContext.clear();
        }

    }

    private String resolveTenant(final HttpServletRequest request) {
        final String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId != null && !tenantId.isBlank()) {
            return tenantId.trim().toLowerCase();
        }
        return null;
    }

}