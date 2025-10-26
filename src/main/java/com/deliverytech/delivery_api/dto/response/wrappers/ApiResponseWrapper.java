package com.deliverytech.delivery_api.dto.response.wrappers;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ApiResponseWrapper<T> {

    private final Boolean success;
    private final T data;
    private final String message;
    private final Instant timestamp;

    private ApiResponseWrapper(T data, String message) {
        this.success = true;
        this.data = data;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponseWrapper<T> of(T data) {
        return new ApiResponseWrapper<>(data, "Operação realizada com sucesso");
    }

    public static <T> ApiResponseWrapper<T> of(T data, String message) {
        return new ApiResponseWrapper<>(data, message);
    }
}
