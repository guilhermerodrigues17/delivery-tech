package com.deliverytech.delivery_api.dto.request;

import com.deliverytech.delivery_api.config.jackson.StrictBooleanDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RestaurantStatusUpdateDto {
    @NotNull(message = "O campo 'active' é obrigatório e deve ser 'true' ou 'false'")
    @JsonDeserialize(using = StrictBooleanDeserializer.class)
    private Boolean active;
}
