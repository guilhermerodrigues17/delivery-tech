package com.deliverytech.delivery_api.dto.request;

import com.deliverytech.delivery_api.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequestDto {
    @NotNull(message = "O status é obrigatório")
    private OrderStatus status;
}
