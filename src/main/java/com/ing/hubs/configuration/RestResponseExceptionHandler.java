package com.ing.hubs.configuration;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.ing.hubs.exception.ResponseException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestResponseExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleInvalidArgument(final MethodArgumentNotValidException ex) {
        final Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });
        return errorMap;
    }

    @ExceptionHandler(value = {ResponseException.class})
    protected ResponseEntity<ExceptionBody> handleResponseException(final ResponseException exception) {
        final var body = new ExceptionBody(exception.getMessage());
        return new ResponseEntity<>(body, exception.getHttpStatus());
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    protected ResponseEntity<ExceptionBody>
    handleUnrecognizedPropertyException(final UnrecognizedPropertyException ex) {
        final String propertyName = ex.getPropertyName();
        final String errorMessage = String.format("Unrecognized property: '%s'", propertyName);
        final var body = new ExceptionBody(errorMessage);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<ExceptionBody> handleJsonMappingException(final JsonMappingException ex) {
        final String errorMessage = ex.getCause().getMessage();
        final var body = new ExceptionBody(errorMessage);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<ExceptionBody> handleJsonParseException(final JsonParseException ex) {
        final String errorMessage = "Invalid request body or JSON format: " + ex.getMessage();
        final var body = new ExceptionBody(errorMessage);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ExceptionBody> handleMissingRequestHeader(final MissingRequestHeaderException ex) {
        final String errorMessage = "Missing request header: " + ex.getMessage();
        final var body = new ExceptionBody(errorMessage);
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionBody> handleMissingRequestHeader(final HttpRequestMethodNotSupportedException ex) {
        final String errorMessage = "Method not allowed: " + ex.getMessage();
        final var body = new ExceptionBody(errorMessage);
        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionBody> handleArgumentMismatch(final MethodArgumentTypeMismatchException ex) {
        final String errorMessage = "Argument mismatch: " + ex.getMessage();
        final var body = new ExceptionBody(errorMessage);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionBody> handleNoRequestBody(final HttpMessageNotReadableException ex) {
        final String errorMessage = "Invalid request body or JSON format: " + ex.getMessage();
        final var body = new ExceptionBody(errorMessage);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @Data
    @AllArgsConstructor
    static class ExceptionBody {
        private String message;
    }
}