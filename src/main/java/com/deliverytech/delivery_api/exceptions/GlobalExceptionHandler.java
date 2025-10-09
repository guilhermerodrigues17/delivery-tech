package com.deliverytech.delivery_api.exceptions;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    private ResponseEntity<ErrorMessage> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        var errorBody = new ErrorMessage();
        errorBody.setMessage(ex.getMessage());
        errorBody.setStatusCode(HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<List<ErrorMessage>> handleValidationException(
            MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream().map(error -> {
            var errorMessage = new ErrorMessage();
            errorMessage.setMessage(error.getDefaultMessage());
            errorMessage.setStatusCode(HttpStatus.BAD_REQUEST.value());
            return errorMessage;
        }).toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(DuplicatedRegisterException.class)
    private ResponseEntity<ErrorMessage> handleDuplicatedRegisterException(
            DuplicatedRegisterException ex) {
        var errorBody = new ErrorMessage();
        errorBody.setMessage(ex.getMessage());
        errorBody.setStatusCode(HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody);
    }

    @ExceptionHandler(NotAllowedException.class)
    private ResponseEntity<ErrorMessage> handleNotAllowedException(NotAllowedException ex) {
        var errorBody = new ErrorMessage();
        errorBody.setMessage(ex.getMessage());
        errorBody.setStatusCode(HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
    }

    @ExceptionHandler(IllegalStateException.class)
    private ResponseEntity<ErrorMessage> handleIllegalStateException(IllegalStateException ex) {
        var errorBody = new ErrorMessage();
        errorBody.setMessage(ex.getMessage());
        errorBody.setStatusCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<ErrorMessage> handleGenericException(Exception ex) {
        var errorBody = new ErrorMessage();
        errorBody.setMessage("Erro interno do servidor");
        errorBody.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
}
