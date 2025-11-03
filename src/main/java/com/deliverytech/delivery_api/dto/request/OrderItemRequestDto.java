package com.deliverytech.delivery_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(description = "DTO de criação de itens do pedido")
public class OrderItemRequestDto {
    @Schema(description = "ID do produto", example = "493897d6-f2f7-42a5-8847-a6335b6604f9")
    @NotNull(message = "O ID do produto é obrigatório")
    private UUID productId;

    @Schema(description = "Quantidade")
    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser um número maior do que zero")
    private Integer quantity;
}
