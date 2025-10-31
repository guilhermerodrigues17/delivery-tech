package com.deliverytech.delivery_api.dto.request;

import com.deliverytech.delivery_api.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "DTO de requisição para registrar um novo usuário")
public class RegisterUserRequestDto {

    @Schema(description = "Nome do usuário", example = "Guilherme")
    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @Schema(description = "E-mail do usuário", example = "name@email.com")
    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    private String email;

    @Schema(description = "Senha do usuário", example = "strongpass123")
    @NotBlank(message = "Senha é obrigatória")
    private String password;

    @Schema(description = "Tipo de permissão do usuário dentro do sistema", example = "ADMIN")
    @NotNull(message = "Role é obrigatória")
    private Role role;

    @Schema(description = "ID do restaurante, usado no caso do usuário ter a role 'RESTAURANT'")
    private UUID restaurantId;
}
