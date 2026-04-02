package com.inventory.engine.exception;

import com.inventory.engine.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ErrorResponse.Violation> violations = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.Violation(error.getField(), error.getDefaultMessage()))
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ErrorResponse.Violation> violations = ex.getConstraintViolations()
                .stream()
                .map(v -> new ErrorResponse.Violation(v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), violations);
    }

    @ExceptionHandler({
            OrderNotFoundException.class,
            WarehouseNotFoundException.class,
            ProductNotFoundException.class,
            StockNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler({
            NotEnoughStockException.class,
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path,
            List<ErrorResponse.Violation> violations
    ) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .violations((violations == null || violations.isEmpty()) ? null : violations)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
