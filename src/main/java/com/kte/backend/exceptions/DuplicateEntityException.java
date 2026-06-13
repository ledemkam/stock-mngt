package com.kte.backend.exceptions;

public class DuplicateEntityException extends RuntimeException {

    public DuplicateEntityException(String message) {
        super(message);
    }

    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateEntityException(Throwable cause) {
        super(cause);
    }

    public DuplicateEntityException(String message, Throwable cause,
                                 boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}