package com.deliverytech.delivery_api.dto.response;

import com.deliverytech.delivery_api.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "DTO de resposta para o registro de um novo usuário")
public record RegisterResponseDto(UUID id, String name, String email, Role role) {
}
