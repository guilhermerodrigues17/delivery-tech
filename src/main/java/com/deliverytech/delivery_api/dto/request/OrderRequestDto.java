package com.deliverytech.delivery_api.dto.request;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequestDto {
    @NotNull(message = "O ID do cliente é obrigatório")
    private UUID consumerId;

    @NotNull(message = "O ID do restaurante é obrigatório")
    private UUID restaurantId;

    @NotEmpty(message = "A lista de itens não pode ser vazia")
    private List<OrderItemRequestDto> items;
}
