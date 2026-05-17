package com.kte.backend.exceptions;


public class DuplicateProductException extends RuntimeException {
    public DuplicateProductException() {
    }

    public DuplicateProductException(String message) {
        super(message);
    }

    public DuplicateProductException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateProductException(Throwable cause) {
        super(cause);
    }

    public DuplicateProductException(String message, Throwable cause,
                                     boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
