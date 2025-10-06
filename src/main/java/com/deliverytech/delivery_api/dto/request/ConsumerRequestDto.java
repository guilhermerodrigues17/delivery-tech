package com.deliverytech.delivery_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConsumerRequestDto {
    @NotBlank(message = "Nome não pode estar em branco")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @NotBlank(message = "E-mail não pode estar em branco")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "Telefone não pode estar em branco")
    @Size(min = 8, max = 15, message = "Telefone deve ter entre 8 e 20 caracteres")
    private String phoneNumber;

    @NotBlank(message = "Endereço é obrigatório")
    private String address;
}
