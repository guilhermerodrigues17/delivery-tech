package com.deliverytech.delivery_api.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequestDto {
    @NotNull(message = "O ID do produto é obrigatório")
    private UUID productId;

    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "Deve ser um número maior do que zero")
    private Integer quantity;
}
