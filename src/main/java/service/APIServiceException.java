package service;

/**
 * Generic runtime exception for APIService errors.
 */
public class APIServiceException extends RuntimeException {
    public APIServiceException(String message) {
        super(message);
    }

    public APIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
