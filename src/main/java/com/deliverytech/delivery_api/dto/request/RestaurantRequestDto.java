package com.deliverytech.delivery_api.dto.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RestaurantRequestDto {
    @NotBlank(message = "Nome não pode estar em branco")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @NotBlank(message = "Categoria não pode estar em branco")
    private String category;

    @NotBlank(message = "Endereço é obrigatório")
    private String address;

    @NotBlank(message = "Telefone não pode estar em branco")
    @Size(min = 8, max = 15, message = "Telefone deve ter entre 8 e 15 caracteres")
    private String phoneNumber;

    @NotNull(message = "Taxa de entrega é obrigatória")
    private BigDecimal deliveryTax;
}
