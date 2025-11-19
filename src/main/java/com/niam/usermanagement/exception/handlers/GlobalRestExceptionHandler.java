package com.niam.usermanagement.exception.handlers;

import com.niam.common.model.response.ErrorResponse;
import com.niam.usermanagement.exception.TokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalRestExceptionHandler {
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponse> handleToken(TokenException ex) {
        ErrorResponse err = ErrorResponse.builder()
                .responseCode(HttpStatus.UNAUTHORIZED.value())
                .reasonCode(HttpStatus.UNAUTHORIZED.series().value())
                .responseDescription(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Set<ErrorResponse> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(f -> ErrorResponse.builder()
                        .responseDescription(f.getField() + ": " + f.getDefaultMessage())
                        .build())
                .collect(Collectors.toSet());

        ErrorResponse err = ErrorResponse.builder()
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .reasonCode(HttpStatus.BAD_REQUEST.series().value())
                .responseDescription("Validation failed")
                .errorResponses(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        ErrorResponse err = ErrorResponse.builder()
                .responseCode(HttpStatus.BAD_REQUEST.value())
                .reasonCode(HttpStatus.BAD_REQUEST.series().value())
                .responseDescription("Malformed JSON")
                .build();
        return ResponseEntity.badRequest().body(err);
    }
}