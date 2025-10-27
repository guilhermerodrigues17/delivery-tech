package com.deliverytech.delivery_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Schema(description = "DTO para criação de um produto")
public class ProductRequestDto {
    @Schema(description = "ID do restaurante", example = "6513d0cd-11e5-45bb-b01a-967082114a09")
    @NotNull(message = "O ID do restaurante é obrigatório")
    private UUID restaurantId;

    @Schema(description = "Nome do produto", example = "Lasanha")
    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(min = 5, max = 50, message = "O nome do produto deve ter entre 5 e 50 caracteres")
    private String name;

    @Schema(description = "Descrição do produto", example = "Massa ao molho bolonhesa, gratinada no forno.")
    @NotBlank(message = "A descrição do produto é obrigatória")
    @Size(min = 10, max = 200,
            message = "A descrição do produto deve ter entre 10 e 200 caracteres")
    private String description;

    @Schema(description = "Preço do produto", example = "39.99")
    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @DecimalMax(value = "500.00", message = "Preço não pode exceder R$500.00")
    private BigDecimal price;

    @Schema(description = "Categoria do produto", example = "Massas")
    @NotBlank(message = "A categoria do produto é obrigatória")
    private String category;

    @Schema(description = "Disponibilidade do produto", example = "true")
    @NotNull(message = "A disponibilidade do produto é obrigatória")
    private Boolean available;
}
