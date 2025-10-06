package com.deliverytech.delivery_api.exceptions;

import lombok.Data;

@Data
public class ErrorMessage {
    private String message;
    private int statusCode;
}
