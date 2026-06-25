package com.wardrove.app.common;

import com.wardrove.app.auth.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    ResponseEntity<ApiErrorResponse> handleAuthException(AuthException exception) {
        HttpStatus status = "AUTH_EMAIL_ALREADY_EXISTS".equals(exception.getError())
                ? HttpStatus.CONFLICT
                : HttpStatus.UNAUTHORIZED;

        return ResponseEntity.status(status).body(new ApiErrorResponse(
                exception.getError(),
                exception.getMessage(),
                status.value(),
                Instant.now()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                "VALIDATION_ERROR",
                "Request validation failed.",
                status.value(),
                Instant.now()
        ));
    }
}
