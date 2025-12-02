package org.finsible.backend;

import org.apache.coyote.BadRequestException;
import org.finsible.backend.CustomExceptionHandler.EntityNotFoundException;
import org.finsible.backend.CustomExceptionHandler.InvalidTokenException;
import org.finsible.backend.CustomExceptionHandler.UserNotFoundException;
import org.finsible.backend.filter.RequestIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler{
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleAuthenticationException(AuthenticationException ex) {
        String traceId = currentTraceId();
        logger.error("Authentication failed (refId={}): {}", traceId, ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.UNAUTHORIZED_REQUEST, "", formatTraceIdForError(traceId));
        return ResponseEntity.status(AppConstants.UNAUTHORIZED_REQUEST)
                .body(new BaseResponse<>(AppConstants.UNAUTHORIZED_REQUEST_MESSAGE, false, error));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleAccessDeniedException(AccessDeniedException ex) {
        String traceId = currentTraceId();
        logger.error("Access denied (refId={}): {}", traceId, ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.FORBIDDEN_REQUEST, "", formatTraceIdForError(traceId));
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new BaseResponse<>(AppConstants.FORBIDDEN_REQUEST_MESSAGE, false, error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String traceId = currentTraceId();
        // This exception contains multiple errors
        List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();

        // Log all errors with same trace ID
        for (ObjectError error : allErrors) {
            logger.error("Validation error (refId={}): {}", traceId, error.getDefaultMessage());
        }
        String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        ErrorDetails error = new ErrorDetails(AppConstants.BAD_REQUEST, errorMessage, formatTraceIdForError(traceId));
        return ResponseEntity.badRequest().body(new BaseResponse<>(AppConstants.BAD_REQUEST_MESSAGE, false, error));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleEntityNotFoundException(EntityNotFoundException ex) {
        String traceId = currentTraceId();
        logger.error("Entity does not exist - (refId={}): {}", traceId, ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.BAD_REQUEST, ex.getMessage(), formatTraceIdForError(traceId));
        return ResponseEntity.badRequest().body(new BaseResponse<>(AppConstants.BAD_REQUEST_MESSAGE, false, error));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleBadRequestException(BadRequestException ex) {
        String traceId = currentTraceId();
        logger.error("{} - (refId={}): {}", AppConstants.BAD_REQUEST_MESSAGE, traceId, ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.BAD_REQUEST, ex.getMessage(), formatTraceIdForError(traceId));
        return ResponseEntity.badRequest().body(new BaseResponse<>(AppConstants.BAD_REQUEST_MESSAGE, false, error));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleInvalidTokenException(InvalidTokenException ex) {
        String traceId = currentTraceId();
        logger.error("Invalid token - (refId={}): {}", traceId, ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.UNAUTHORIZED_REQUEST, ex.getMessage(), formatTraceIdForError(traceId));
        return ResponseEntity.status(AppConstants.UNAUTHORIZED_REQUEST)
                .body(new BaseResponse<>(AppConstants.UNAUTHORIZED_REQUEST_MESSAGE, false, error));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleUserNotFoundException(UserNotFoundException ex) {
        String traceId = currentTraceId();
        logger.error("{} - (refId={}): {}", AppConstants.USER_NOT_FOUND, traceId, ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.UNAUTHORIZED_REQUEST, ex.getMessage(), formatTraceIdForError(traceId));
        return ResponseEntity.status(AppConstants.UNAUTHORIZED_REQUEST)
                .body(new BaseResponse<>(AppConstants.USER_NOT_FOUND, false, error));
    }

    @ExceptionHandler({GeneralSecurityException.class, IOException.class})
    public ResponseEntity<BaseResponse<ErrorDetails>> handleSecurityException(Exception ex) {
        String traceId = currentTraceId();
        logger.error("Security/IO exception - (refId={}): {}", traceId, ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.INTERNAL_SERVER_ERROR, "Security/IO exception", formatTraceIdForError(traceId));
        return ResponseEntity.status(AppConstants.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse<>(AppConstants.INTERNAL_SERVER_ERROR_MESSAGE, false, error));
    }

    @ExceptionHandler(RuntimeException.class)
    public  ResponseEntity<BaseResponse<ErrorDetails>> handleRuntimeException(RuntimeException ex) {
        String traceId = currentTraceId();
        logger.error("Runtime error - (refId={}): {}", traceId, ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.INTERNAL_SERVER_ERROR, "Runtime error", formatTraceIdForError(traceId));
        return ResponseEntity.status(AppConstants.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse<>(AppConstants.INTERNAL_SERVER_ERROR_MESSAGE, false, error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<ErrorDetails>> handleGenericException(Exception ex) {
        String traceId = currentTraceId();
        logger.error("Unexpected error - (refId={}): {}", traceId, ex.getMessage(), ex);
        ErrorDetails error = new ErrorDetails(AppConstants.INTERNAL_SERVER_ERROR, "Unexpected error occurred", formatTraceIdForError(traceId));
        return ResponseEntity.status(AppConstants.INTERNAL_SERVER_ERROR)
                .body(new BaseResponse<>(AppConstants.INTERNAL_SERVER_ERROR_MESSAGE, false, error));
    }

    private String currentTraceId() {
        String traceId = MDC.get(RequestIdFilter.TRACE_ID_KEY);
        return (traceId == null || traceId.isBlank()) ? UUID.randomUUID().toString() : traceId;
    }

    private String formatTraceIdForError(String traceId) {
        return "refId:" + traceId;
    }
}
