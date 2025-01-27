package org.finsible.backend;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseResponse<T> {
    private String message;
    private boolean success;
    private int status;
    private T data;

    public BaseResponse() {
    }

    public BaseResponse(String message, boolean success, int status) {
        this.message = message;
        this.success = success;
        this.status = status;
    }

    public BaseResponse(String message, boolean success, int status, T data) {
        this.message = message;
        this.success = success;
        this.status = status;
        this.data = data;
    }
}
