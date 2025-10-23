package com.deliverytech.delivery_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO para criar ou atualizar um cliente")
public class ConsumerRequestDto {
    @Schema(description = "Nome completo", example = "João da Silva")
    @NotBlank(message = "Nome não pode estar em branco")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @Schema(description = "E-mail", example = "joao@email.com")
    @NotBlank(message = "E-mail não pode estar em branco")
    @Email(message = "E-mail inválido")
    private String email;

    @Schema(description = "Telefone", example = "11911223344")
    @NotBlank(message = "Telefone não pode estar em branco")
    @Size(min = 8, max = 15, message = "Telefone deve ter entre 8 e 15 caracteres")
    private String phoneNumber;

    @Schema(description = "Endereço", example = "Rua A, 1000")
    @NotBlank(message = "Endereço é obrigatório")
    private String address;
}
