package com.estore.userservice.common.exception;

import com.estore.userservice.user.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // request DTO validation exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleRequestBeansValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, List<String>> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.computeIfAbsent(error.getField(), l -> new ArrayList<>())
                                        .add(error.getDefaultMessage())
                );

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "request data validation failed",
                errors,
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(response);
    }

    // request path param validation exception
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleRequestParamValidationViolation(
            HandlerMethodValidationException ex) {

        Map<String, List<String>> errors = new HashMap<>();
        ex.getParameterValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();

            result.getResolvableErrors().forEach(error ->
                    errors.computeIfAbsent(paramName, l -> new ArrayList<>())
                            .add(error.getDefaultMessage())
            );
        });

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "request param validation failed",
                errors,
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "invalid email or password",
                Map.of(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    // fallback handler for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "internal server error",
                Map.of(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
