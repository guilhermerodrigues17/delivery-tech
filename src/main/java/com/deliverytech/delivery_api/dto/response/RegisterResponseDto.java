package com.deliverytech.delivery_api.dto.response;

import com.deliverytech.delivery_api.model.enums.Role;

import java.util.UUID;

public record RegisterResponseDto(UUID id, String name, String email, Role role) {
}
