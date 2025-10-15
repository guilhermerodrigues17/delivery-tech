package com.deliverytech.delivery_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class OrderRequestDto {
    @NotNull(message = "O ID do cliente é obrigatório")
    private UUID consumerId;

    @NotNull(message = "O ID do restaurante é obrigatório")
    private UUID restaurantId;

    @Valid
    @NotEmpty(message = "A lista de itens não pode ser vazia")
    private List<OrderItemRequestDto> items;
}
