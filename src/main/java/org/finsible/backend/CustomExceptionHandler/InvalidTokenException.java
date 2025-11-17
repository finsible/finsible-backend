package org.finsible.backend.CustomExceptionHandler;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
