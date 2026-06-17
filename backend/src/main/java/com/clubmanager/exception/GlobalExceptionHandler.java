package com.clubmanager.exception;

import com.clubmanager.service.AppMetricsService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final AppMetricsService appMetricsService;



    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException exception) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        recordValidationFailure("constraint_violation", exception.getMessage());
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Request validation failed");
        recordValidationFailure("request_body", message);
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        recordValidationFailure("business_rule", exception.getMessage());
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    ResponseEntity<ErrorResponse> handleAuthorizationDenied(AuthorizationDeniedException exception) {
        appMetricsService.recordAccessDenied();
        LOGGER.warn("Access denied");
        return response(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied");
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException exception) {
        appMetricsService.recordLoginFailure();
        LOGGER.warn("Authentication failed with bad credentials");
        return response(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Invalid username or password");
    }

    @ExceptionHandler(LoginRateLimitException.class)
    ResponseEntity<ErrorResponse> handleLoginRateLimit(LoginRateLimitException exception) {
        return response(HttpStatus.TOO_MANY_REQUESTS, "LOGIN_RATE_LIMITED", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleGeneric(Exception exception) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", exception.getMessage());
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(error, message, MDC.get("traceId")));
    }

    private void recordValidationFailure(String type, String message) {
        appMetricsService.recordValidationFailure(type);
        LOGGER.warn("Validation failed type={} message={}", type, message);
    }
}
