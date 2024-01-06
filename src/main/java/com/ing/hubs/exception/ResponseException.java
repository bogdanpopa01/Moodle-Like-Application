package com.ing.hubs.exception;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class ResponseException extends RuntimeException{
    private HttpStatus httpStatus;
    private String message;
}
