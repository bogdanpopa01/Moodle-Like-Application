package com.ing.hubs.exception;

import com.ing.hubs.configuration.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;


@Slf4j
public class DuplicateDataException extends ResponseException {
    public DuplicateDataException(final DataIntegrityViolationException ex) {
        super.setHttpStatus(HttpStatus.BAD_REQUEST);
        super.setMessage(String.format("The %s you entered already exists", matchField(ex.getMessage())));
    }

    private String matchField(final String exceptionMessage){
        if (exceptionMessage.contains(Constant.DUPLICATE_EMAIL)){
            return "email";
        }
        if (exceptionMessage.contains(Constant.DUPLICATE_PHONE_NUMBER)){
            return "phone number";
        }
        if (exceptionMessage.contains(Constant.DUPLICATE_USERNAME)){
            return "username";
        }
        if (exceptionMessage.contains(Constant.DUPLICATE_COURSE_NAME)){
            return "course name";
        }
        throw new RuntimeException("An unknown error occurred!");
    }
}
