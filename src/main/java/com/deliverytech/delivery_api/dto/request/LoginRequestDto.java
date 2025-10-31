package com.deliverytech.delivery_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO de requisição do login")
public class LoginRequestDto {

    @Schema(description = "E-mail do usuário", example = "name@email.com")
    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail deve ser válido")
    private String email;

    @Schema(description = "Senha do usuário", example = "strongpass123")
    @NotBlank(message = "Senha é obrigatória")
    private String password;
}
