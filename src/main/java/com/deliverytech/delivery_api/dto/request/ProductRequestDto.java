package com.deliverytech.delivery_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductRequestDto {
    @NotNull(message = "O ID do restaurante é obrigatório")
    private UUID restaurantId;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(min = 5, max = 50, message = "O nome do produto deve ter entre 5 e 50 caracteres")
    private String name;

    @NotBlank(message = "A descrição do produto é obrigatória")
    @Size(min = 10, max = 200,
            message = "A descrição do produto deve ter entre 10 e 200 caracteres")
    private String description;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço do produto deve ser maior que zero")
    private BigDecimal price;

    @NotBlank(message = "A categoria do produto é obrigatória")
    private String category;

    @NotNull(message = "A disponibilidade do produto é obrigatória")
    private Boolean available;
}
