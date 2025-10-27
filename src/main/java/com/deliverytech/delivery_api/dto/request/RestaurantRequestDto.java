package com.deliverytech.delivery_api.dto.request;

import com.deliverytech.delivery_api.validation.annotations.ValidCategory;
import com.deliverytech.delivery_api.validation.annotations.ValidPhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO para cadastrar um restaurante")
public class RestaurantRequestDto {
    @Schema(description = "Nome do restaurante. Deve ser único", example = "Lanchonete C")
    @NotBlank(message = "Nome não pode estar em branco")
    @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
    private String name;

    @Schema(description = "Categoria do restaurante", example = "Italiana")
    @NotBlank(message = "Categoria não pode estar em branco")
    @ValidCategory
    private String category;

    @Schema(description = "Endereço do restaurante", example = "Rua C, 1000")
    @NotBlank(message = "Endereço é obrigatório")
    private String address;

    @Schema(description = "Telefone de contato do restaurante", example = "(11)92233-4455")
    @NotBlank(message = "Telefone não pode estar em branco")
    @ValidPhoneNumber
    private String phoneNumber;

    @Schema(description = "Valor base da taxa de entrega do restaurante", example = "10.00")
    @NotNull(message = "Taxa de entrega é obrigatória")
    @PositiveOrZero(message = "Taxa de entrega deve ser zero ou maior que zero")
    private BigDecimal deliveryTax;
}
