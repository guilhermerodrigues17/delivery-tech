package com.deliverytech.delivery_api.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(description = "DTO para criar um pedido")
public class OrderRequestDto {
    @Schema(description = "ID do cliente", example = "493897d6-f2f7-42a5-8847-a6335b6604f9")
    @NotNull(message = "O ID do cliente é obrigatório")
    private UUID consumerId;

    @Schema(description = "ID do restaurante", example = "493897d6-f2f7-42a5-8847-a6335b6604f9")
    @NotNull(message = "O ID do restaurante é obrigatório")
    private UUID restaurantId;

    @ArraySchema(
            schema = @Schema(
                    description = "Lista de itens do pedido",
                    implementation = OrderItemRequestDto.class
            )
    )
    @Valid
    @NotEmpty(message = "A lista de itens não pode ser vazia")
    private List<OrderItemRequestDto> items;
}
