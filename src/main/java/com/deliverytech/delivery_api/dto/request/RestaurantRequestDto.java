package com.deliverytech.delivery_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO para cadastrar um restaurante")
public class RestaurantRequestDto {
    @Schema(description = "Nome do restaurante. Deve ser único", example = "Lanchonete C")
    @NotBlank(message = "Nome não pode estar em branco")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @Schema(description = "Categoria do restaurante", example = "Italiana")
    @NotBlank(message = "Categoria não pode estar em branco")
    private String category;

    @Schema(description = "Endereço do restaurante", example = "Rua C, 1000")
    @NotBlank(message = "Endereço é obrigatório")
    private String address;

    @Schema(description = "Telefone de contato do restaurante", example = "11922334455")
    @NotBlank(message = "Telefone não pode estar em branco")
    @Size(min = 8, max = 15, message = "Telefone deve ter entre 8 e 15 caracteres")
    private String phoneNumber;

    @Schema(description = "Valor base da taxa de entrega do restaurante", example = "10.00")
    @NotNull(message = "Taxa de entrega é obrigatória")
    private BigDecimal deliveryTax;
}
