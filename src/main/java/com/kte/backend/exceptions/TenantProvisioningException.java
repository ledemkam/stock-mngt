package com.kte.backend.exceptions;

public class TenantProvisioningException extends RuntimeException {
    public TenantProvisioningException(String message) {
        super(message);
    }

    public TenantProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }

    public TenantProvisioningException(Throwable cause) {
        super(cause);
    }

    public TenantProvisioningException(String message, Throwable cause,
                                   boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
