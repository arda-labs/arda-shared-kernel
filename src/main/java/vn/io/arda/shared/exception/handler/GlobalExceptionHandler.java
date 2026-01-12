package vn.io.arda.shared.exception.handler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.io.arda.shared.exception.ArdaException;
import vn.io.arda.shared.exception.InvalidTenantContextException;
import vn.io.arda.shared.exception.TenantAccessDeniedException;
import vn.io.arda.shared.exception.TenantNotFoundException;
import vn.io.arda.shared.exception.model.ErrorResponse;
import vn.io.arda.shared.exception.model.ValidationErrorResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for all Arda platform exceptions.
 * Provides consistent error responses across all services with distributed tracing support.
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Automatic traceId extraction from common tracing headers (Zipkin, Sleuth, custom)</li>
 *   <li>Fallback to generated UUID when no tracing header present</li>
 *   <li>Consistent error response format with {@link ErrorResponse}</li>
 *   <li>Validation error details with {@link ValidationErrorResponse}</li>
 * </ul>
 *
 * @since 0.0.1
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Extracts trace ID from request headers for distributed tracing correlation.
     * Supports multiple tracing systems.
     *
     * @param request the HTTP request
     * @return trace ID from header or generated UUID
     */
    private String extractTraceId(HttpServletRequest request) {
        // Try common tracing headers in order of preference
        String traceId = request.getHeader("X-B3-TraceId"); // Zipkin/Sleuth
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }

        traceId = request.getHeader("X-Trace-Id"); // Custom/APISIX
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }

        traceId = request.getHeader("traceparent"); // W3C Trace Context
        if (traceId != null && !traceId.isBlank()) {
            // Extract trace-id from traceparent format: version-trace-id-parent-id-flags
            String[] parts = traceId.split("-");
            if (parts.length >= 2) {
                return parts[1];
            }
        }

        // Generate new trace ID if not present
        return UUID.randomUUID().toString();
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTenantNotFound(
            TenantNotFoundException ex, HttpServletRequest request) {
        String traceId = extractTraceId(request);
        log.error("[traceId={}] Tenant not found: {}", traceId, ex.getTenantId(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .traceId(traceId)
                .details(new HashMap<>() {{
                    put("tenantId", ex.getTenantId());
                }})
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TenantAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleTenantAccessDenied(
            TenantAccessDeniedException ex, HttpServletRequest request) {
        String traceId = extractTraceId(request);
        log.error("[traceId={}] Tenant access denied: tenant={}, user={}", traceId, ex.getTenantId(), ex.getUserId(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .traceId(traceId)
                .details(new HashMap<>() {{
                    put("tenantId", ex.getTenantId());
                    put("userId", ex.getUserId());
                }})
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidTenantContextException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTenantContext(
            InvalidTenantContextException ex, HttpServletRequest request) {
        String traceId = extractTraceId(request);
        log.error("[traceId={}] Invalid tenant context: {}", traceId, ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        String traceId = extractTraceId(request);
        log.error("[traceId={}] Entity not found: {}", traceId, ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = extractTraceId(request);
        log.error("[traceId={}] Validation error: {}", traceId, ex.getMessage());

        List<ValidationErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ValidationErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .rejectedValue(error.getRejectedValue())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ValidationErrorResponse error = ValidationErrorResponse.validationBuilder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .path(request.getRequestURI())
                .traceId(traceId)
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        String traceId = extractTraceId(request);
        log.error("[traceId={}] Access denied: {}", traceId, ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("Access denied")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        String traceId = extractTraceId(request);
        log.error("[traceId={}] Bad credentials: {}", traceId, ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Invalid credentials")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(ArdaException.class)
    public ResponseEntity<ErrorResponse> handleArdaException(
            ArdaException ex, HttpServletRequest request) {
        String traceId = extractTraceId(request);
        log.error("[traceId={}] Arda exception: {}", traceId, ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        String traceId = extractTraceId(request);
        log.error("[traceId={}] Unexpected error: {}", traceId, ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
