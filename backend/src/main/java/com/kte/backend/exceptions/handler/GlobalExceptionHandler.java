package com.kte.backend.exceptions.handler;

import com.kte.backend.common.PageReponse;
import com.kte.backend.dto.ErrorDto;
import com.kte.backend.exceptions.DuplicateCategoryException;
import com.kte.backend.exceptions.DuplicateProductException;

import com.kte.backend.exceptions.UnauthorizedException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
            AuthenticationException.class,
            BadCredentialsException.class,
            UsernameNotFoundException.class,

    })
    public ResponseEntity<ErrorDto> handleAuthenticationException(Exception ex) {
        log.warn("Authentication failed: {}", ex.getMessage()); // log.warn, not log.error
        log.info("Login or / and password are incorrect");
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("Invalid credentials"); // Generic message for security
        return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("Access denied");
        return new ResponseEntity<>(errorDto, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
                    EntityNotFoundException.class,
                    UsernameNotFoundException.class
    })
    public ResponseEntity<ErrorDto> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request) {
        ErrorDto error = new ErrorDto(
                ex.getMessage(),
                request.getRequestURI(),   // → path
                404,                       // → status
                LocalDateTime.now()        // → timestamp
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }



    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorDto> handleUnauthorizedException(UnauthorizedException ex) {
        log.error("Caught UnauthorizedException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("UNAUTHORIZED");
        return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicateProductException.class)
    public ResponseEntity<ErrorDto> handleDuplicateProductException(DuplicateProductException ex) {
        log.error("Caught DuplicateProductException", ex);
       ErrorDto errorDto = new ErrorDto();
        errorDto.setError("DUPLICATE_PRODUCT");
        return new ResponseEntity<>(errorDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateCategoryException.class)
    public ResponseEntity<ErrorDto> handleDuplicateCategoryException(DuplicateProductException ex) {
        log.error("Caught DuplicateProductException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("DUPLICATE_PRODUCT");
        return new ResponseEntity<>(errorDto, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        log.error("Caught MethodArgumentNotValidException", ex);
        ErrorDto errorDto = new ErrorDto();

        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("Validation error occurred");

        errorDto.setError(errorMessage);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        log.error("Caught ConstraintViolationException", ex);
        ErrorDto errorDto = new ErrorDto();

        String errorMessage = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation ->
                        violation.getPropertyPath() + ": " + violation.getMessage()
                ).orElse("Constraint violation occurred");

        errorDto.setError(errorMessage);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        log.error("Caught exception", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("An unknown error occurred");
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
