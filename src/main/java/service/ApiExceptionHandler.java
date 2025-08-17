package service;

import java.net.http.HttpResponse;

/**
 * Small utility to convert exceptions and non-2xx responses into APIServiceException with
 * user-friendly messages. Keep logic minimal to avoid coupling to UI.
 */
public class ApiExceptionHandler {
    public static APIServiceException fromIOException(java.io.IOException e, String context) {
        return new APIServiceException("Network error while " + context + ": " + e.getMessage(), e);
    }

    public static APIServiceException fromInterruptedException(InterruptedException e, String context) {
        Thread.currentThread().interrupt();
        return new APIServiceException("Operation interrupted while " + context + ": " + e.getMessage(), e);
    }

    public static APIServiceException fromInvalidConfig(String message) {
        return new APIServiceException("Configuration error: " + message);
    }

    public static APIServiceException fromHttpResponse(HttpResponse<?> response, String context) {
        return new APIServiceException(
            "API responded with status " + response.statusCode() + " while " + context + ": " + response.body()
        );
    }
}
