package com.carpool.carpool.exception;

public class InvalidGoogleTokenException extends RuntimeException {
    public InvalidGoogleTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
