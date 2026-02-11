package com.tonvq.accountingerp.application.exception;

/**
 * Data Access Exception
 * Thrown when data access operation fails
 * 
 * @author Ton VQ
 */
public class DataAccessException extends RuntimeException {
    private final String errorCode;

    public DataAccessException(String message) {
        super(message);
        this.errorCode = "DATA_ACCESS_ERROR";
    }

    public DataAccessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DataAccessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
