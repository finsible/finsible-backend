package org.finsible.backend;

import lombok.Data;

@Data
public class ErrorDetails {
    private int errorCode;
    private String message;
    private String details;

    public ErrorDetails(int errorCode, String message, String details) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
    }
}
