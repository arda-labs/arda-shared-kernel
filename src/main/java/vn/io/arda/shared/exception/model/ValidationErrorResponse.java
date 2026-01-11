package vn.io.arda.shared.exception.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * Error response for validation errors.
 *
 * @since 0.0.1
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationErrorResponse extends ErrorResponse {
    private List<FieldError> fieldErrors;

    @Builder(builderMethodName = "validationBuilder")
    public ValidationErrorResponse(java.time.Instant timestamp, int status, String error,
                                   String message, String path, Map<String, Object> details,
                                   List<FieldError> fieldErrors) {
        super(timestamp, status, error, message, path, details);
        this.fieldErrors = fieldErrors;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;
    }
}
