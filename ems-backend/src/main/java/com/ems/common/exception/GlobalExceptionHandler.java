package com.ems.common.exception;

import com.ems.common.dto.ApiResponse;
import com.ems.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse err = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage(), err));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException ex, HttpServletRequest request) {
        ErrorResponse err = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage(), err));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
        ErrorResponse err = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage(), err));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        String msg = "Access Denied: You do not have permission to access this resource.";
        ErrorResponse err = ErrorResponse.builder()
                .message(msg)
                .status(HttpStatus.FORBIDDEN.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(msg, err));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String msg = "Validation failed for request fields.";
        ErrorResponse err = ErrorResponse.builder()
                .message(msg)
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .fields(errors)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg, err));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        ErrorResponse err = ErrorResponse.builder()
                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal Server Error", err));
    }
}
