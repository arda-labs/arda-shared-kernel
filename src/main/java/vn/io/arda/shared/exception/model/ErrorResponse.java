package vn.io.arda.shared.exception.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response format for all API errors.
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
    private Map<String, Object> details;
}
