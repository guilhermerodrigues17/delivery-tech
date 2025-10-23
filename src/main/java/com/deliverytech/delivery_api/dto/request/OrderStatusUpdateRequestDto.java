package com.deliverytech.delivery_api.dto.request;

import com.deliverytech.delivery_api.model.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO de atualização do status de um pedido")
public class OrderStatusUpdateRequestDto {
    @Schema(description = "Status do pedido", example = "OUT_FOR_DELIVERY")
    @NotNull(message = "O status é obrigatório")
    private OrderStatus status;
}
