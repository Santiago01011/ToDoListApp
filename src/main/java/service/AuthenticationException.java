package service;

/**
 * Thrown when authentication is required but missing or invalid.
 */
public class AuthenticationException extends APIServiceException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
