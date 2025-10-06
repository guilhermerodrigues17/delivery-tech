package com.deliverytech.delivery_api.exceptions;

public class DuplicatedRegisterException extends RuntimeException {
    public DuplicatedRegisterException(String message) {
        super(message);
    }
}
