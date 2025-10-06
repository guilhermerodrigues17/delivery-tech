package com.deliverytech.delivery_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    private ResponseEntity<ErrorMessage> handleResourceNotFoundException(ResourceNotFoundException ex) {
        var errorBody = new ErrorMessage();
        errorBody.setMessage(ex.getMessage());
        errorBody.setStatusCode(HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<ErrorMessage> handleGenericException(Exception ex) {
        var errorBody = new ErrorMessage();
        errorBody.setMessage(ex.getMessage());
        errorBody.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
}
