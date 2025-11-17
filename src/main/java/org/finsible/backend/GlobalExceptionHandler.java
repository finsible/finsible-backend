package org.finsible.backend;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.CustomExceptionHandler.EntityNotFoundException;
import org.finsible.backend.CustomExceptionHandler.InvalidTokenException;
import org.finsible.backend.CustomExceptionHandler.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RestControllerAdvice
public class GlobalExceptionHandler{
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logger.error("Validation error: {}", ex.getMessage());
        String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        ErrorDetails error = new ErrorDetails(AppConstants.BAD_REQUEST, errorMessage, ex.getCause() != null ? ex.getCause().toString() : "");
        return ResponseEntity.badRequest().body(new BaseResponse<>(AppConstants.BAD_REQUEST_MESSAGE, false, error));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleEntityNotFoundException(EntityNotFoundException ex) {
        logger.error("Entity does not exist: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(AppConstants.BAD_REQUEST, ex.getMessage(), ex.getCause() != null ? ex.getCause().toString() : "");
        return ResponseEntity.badRequest().body(new BaseResponse<>(AppConstants.BAD_REQUEST_MESSAGE, false, error));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleBadRequestException(BadRequestException ex) {
        logger.error("{}: {}", AppConstants.BAD_REQUEST_MESSAGE, ex.getMessage());
        ErrorDetails error = new ErrorDetails(AppConstants.BAD_REQUEST, ex.getMessage(), ex.getCause() != null ? ex.getCause().toString() : "");
        return ResponseEntity.badRequest().body(new BaseResponse<>(AppConstants.BAD_REQUEST_MESSAGE, false, error));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleInvalidTokenException(InvalidTokenException ex) {
        logger.error("Invalid token: {}", ex.getMessage());
        ErrorDetails error = new ErrorDetails(AppConstants.UNAUTHORIZED_REQUEST, ex.getMessage(), ex.getCause() != null ? ex.getCause().toString() : "");
        return ResponseEntity.status(AppConstants.UNAUTHORIZED_REQUEST)
                .body(new BaseResponse<>(AppConstants.UNAUTHORIZED_REQUEST_MESSAGE, false, error));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleUserNotFoundException(UserNotFoundException ex) {
        logger.error("{}: {}",AppConstants.USER_NOT_FOUND, ex.getMessage());
        ErrorDetails error = new ErrorDetails(AppConstants.UNAUTHORIZED_REQUEST, ex.getMessage(), ex.getCause() != null ? ex.getCause().toString() : "");
        return ResponseEntity.status(AppConstants.UNAUTHORIZED_REQUEST)
                .body(new BaseResponse<>(AppConstants.USER_NOT_FOUND, false, error));
    }

    @ExceptionHandler({GeneralSecurityException.class, IOException.class})
    public ResponseEntity<BaseResponse<ErrorDetails>> handleSecurityException(Exception ex) {
        logger.error("Security/IO exception: {}", ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getCause() != null ? ex.getCause().toString() : "");
        return ResponseEntity.status(AppConstants.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse<>(AppConstants.INTERNAL_SERVER_ERROR_MESSAGE, false, error));
    }

    @ExceptionHandler(RuntimeException.class)
    public  ResponseEntity<BaseResponse<ErrorDetails>> handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime error: {}", ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getCause() != null ? ex.getCause().toString() : "");
        return ResponseEntity.status(AppConstants.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse<>(AppConstants.INTERNAL_SERVER_ERROR_MESSAGE, false, error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getCause() != null ? ex.getCause().toString() : "");
        return ResponseEntity.status(AppConstants.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse<>(AppConstants.INTERNAL_SERVER_ERROR_MESSAGE, false, error));
    }
}
