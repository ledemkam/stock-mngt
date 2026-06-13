package com.kte.backend.exceptions.handler;

import com.kte.backend.dto.ErrorDto;
import com.kte.backend.exceptions.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    // Pas de dépendances injectées → instantiation directe, aucun contexte Spring requis
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // ---- 401 Authentication ----

    @Test
    void handleAuthenticationException_shouldReturn401_forAuthenticationException() {
        AuthenticationException ex = mock(AuthenticationException.class);

        ResponseEntity<ErrorDto> response = handler.handleAuthenticationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError()).isEqualTo("Invalid credentials");
    }

    @Test
    void handleAuthenticationException_shouldReturn401_forBadCredentialsException() {
        ResponseEntity<ErrorDto> response = handler.handleAuthenticationException(
                new BadCredentialsException("bad creds"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError()).isEqualTo("Invalid credentials");
    }

    @Test
    void handleAuthenticationException_shouldReturn401_forUsernameNotFoundException() {
        ResponseEntity<ErrorDto> response = handler.handleAuthenticationException(
                new UsernameNotFoundException("user not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError()).isEqualTo("Invalid credentials");
    }

    @Test
    void handleAuthenticationException_shouldReturnGenericMessage_notLeakOriginalReason() {
        // Sécurité : le message renvoyé ne doit PAS contenir le login ou la raison réelle
        ResponseEntity<ErrorDto> response = handler.handleAuthenticationException(
                new BadCredentialsException("user 'john' does not exist"));

        assertThat(response.getBody().getError()).doesNotContain("john");
    }

    // ---- 403 Access Denied ----

    @Test
    void handleAccessDenied_shouldReturn403_withAccessDeniedMessage() {
        ResponseEntity<ErrorDto> response = handler.handleAccessDenied(
                new AccessDeniedException("forbidden resource"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getError()).isEqualTo("Access denied");
    }

    // ---- 404 Entity Not Found ----

    @Test
    void handleEntityNotFound_shouldReturn404_withExceptionMessageAsError() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/products/999");

        ResponseEntity<ErrorDto> response = handler.handleEntityNotFound(
                new EntityNotFoundException("Product not found"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isEqualTo("Product not found");
    }

    @Test
    void handleEntityNotFound_shouldPopulatePath_fromRequestUri() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/categories/42");

        ResponseEntity<ErrorDto> response = handler.handleEntityNotFound(
                new EntityNotFoundException("Category not found"), request);

        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/categories/42");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    // ---- 401 Unauthorized (JWT) ----

    @Test
    void handleUnauthorizedException_shouldReturn401_withGenericUnauthorizedCode() {
        ResponseEntity<ErrorDto> response = handler.handleUnauthorizedException(
                new UnauthorizedException("JWT expired"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    void handleUnauthorizedException_shouldNotLeakJwtDetails() {
        ResponseEntity<ErrorDto> response = handler.handleUnauthorizedException(
                new UnauthorizedException("signature does not match key=RS256"));

        assertThat(response.getBody().getError()).doesNotContain("RS256");
    }

    // ---- 500 Tenant Provisioning ----

    @Test
    void handleTenantProvisioningException_shouldReturn500_withCauseMessageAppended() {
        ResponseEntity<ErrorDto> response = handler.handleTenantProvisioningException(
                new TenantProvisioningException("DB unreachable"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError())
                .isEqualTo("Failed to provision tenant: DB unreachable");
    }

    // ---- 400 Invalid Request ----

    @Test
    void handleInvalidRequestException_shouldReturn400_withInvalidRequestCode() {
        ResponseEntity<ErrorDto> response = handler.handleInvalidRequestException(
                new InvalidRequestException("role PLATFORM_ADMIN not allowed"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("INVALID_REQUEST");
    }

    // ---- 409 Duplicate Entity ----

    @Test
    void handleDuplicateEntityException_shouldReturn409_withExceptionMessageVerbatim() {
        ResponseEntity<ErrorDto> response = handler.handleDuplicateEntityException(
                new DuplicateEntityException("Username 'john' already taken"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError()).isEqualTo("Username 'john' already taken");
    }

    // ---- 400 Validation — MethodArgumentNotValid ----

    @Test
    void handleMethodArgumentNotValidException_shouldReturn400_withFirstFieldError() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(new FieldError("registerDto", "username", "must not be blank")));

        ResponseEntity<ErrorDto> response = handler.handleMethodArgumentNotValidException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("username: must not be blank");
    }

    @Test
    void handleMethodArgumentNotValidException_shouldReturnFallback_whenNoFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ErrorDto> response = handler.handleMethodArgumentNotValidException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("Validation error occurred");
    }

    @Test
    void handleMethodArgumentNotValidException_shouldUseOnlyFirstError_whenMultipleFieldsFail() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("dto", "email", "invalid email"),
                new FieldError("dto", "username", "must not be blank")));

        ResponseEntity<ErrorDto> response = handler.handleMethodArgumentNotValidException(ex);

        // Seule la première erreur est retournée
        assertThat(response.getBody().getError()).isEqualTo("email: invalid email");
    }

    // ---- 400 Validation — ConstraintViolation ----

    @Test
    @SuppressWarnings("unchecked")
    void handleConstraintViolation_shouldReturn400_withPropertyAndMessage() {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("email");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        ResponseEntity<ErrorDto> response = handler.handleConstraintViolation(
                new ConstraintViolationException(Set.of(violation)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("email: must not be blank");
    }

    @Test
    void handleConstraintViolation_shouldReturnFallback_whenViolationSetIsEmpty() {
        ResponseEntity<ErrorDto> response = handler.handleConstraintViolation(
                new ConstraintViolationException(Set.of()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("Constraint violation occurred");
    }

    // ---- 500 Generic ----

    @Test
    void handleException_shouldReturn500_withGenericMessage() {
        ResponseEntity<ErrorDto> response = handler.handleException(
                new RuntimeException("unexpected NullPointerException"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError()).isEqualTo("An unknown error occurred");
    }

    @Test
    void handleException_shouldNotLeakInternalDetails() {
        // Le message interne ne doit jamais être exposé dans la réponse HTTP
        ResponseEntity<ErrorDto> response = handler.handleException(
                new RuntimeException("DB password=secret123"));

        assertThat(response.getBody().getError()).doesNotContain("secret123");
    }
}