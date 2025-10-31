package com.deliverytech.delivery_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de resposta para o login de um usuário, contendo o token JWT gerado")
public record LoginResponseDto(String token) {}
