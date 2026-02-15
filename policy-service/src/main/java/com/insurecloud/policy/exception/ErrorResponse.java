package com.insurecloud.policy.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * A standard error response object returned by the API when an exception occurs.
 *
 * @param status    The HTTP status code.
 * @param message   A human-readable error message.
 * @param timestamp The time the error occurred.
 * @param errors    A map of field-specific validation errors, if any.
 */
public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> errors
) {
    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now(), null);
    }
}
