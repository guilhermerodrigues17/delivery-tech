package com.deliverytech.delivery_api.dto.response;

import java.math.BigDecimal;

public record OrderTotalResponseDto(
        BigDecimal subtotal,
        BigDecimal deliveryTax,
        BigDecimal total
) {
}
