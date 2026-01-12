package vn.io.arda.shared.exception.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response format for all API errors.
 * Includes distributed tracing support via traceId.
 *
 * <p><strong>Fields:</strong></p>
 * <ul>
 *   <li>{@code timestamp} - When the error occurred (ISO-8601 format)</li>
 *   <li>{@code status} - HTTP status code (e.g., 404, 500)</li>
 *   <li>{@code error} - HTTP status reason phrase (e.g., "Not Found")</li>
 *   <li>{@code message} - Human-readable error message</li>
 *   <li>{@code path} - Request URI that caused the error</li>
 *   <li>{@code traceId} - Distributed tracing ID for correlating logs across services</li>
 *   <li>{@code details} - Additional error context (optional)</li>
 * </ul>
 *
 * <p><strong>Example JSON Response:</strong></p>
 * <pre>
 * {
 *   "timestamp": "2026-01-12T10:30:00Z",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Tenant not found: tenant-123",
 *   "path": "/api/users",
 *   "traceId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
 *   "details": {
 *     "tenantId": "tenant-123"
 *   }
 * }
 * </pre>
 *
 * @since 0.0.1
 */
@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String traceId;
    private Map<String, Object> details;
}
