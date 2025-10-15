package com.deliverytech.delivery_api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponseDto(
        UUID id,
        UUID productId,
        String productName,
        Integer quantity,
        BigDecimal subtotal) {

}
