package com.deliverytech.delivery_api.dto.request;

import com.deliverytech.delivery_api.config.jackson.StrictBooleanDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO de atualização do status do restaurante")
public class RestaurantStatusUpdateDto {
    @Schema(description = "Status do restaurante", example = "true")
    @NotNull(message = "O campo 'active' é obrigatório e deve ser 'true' ou 'false'")
    @JsonDeserialize(using = StrictBooleanDeserializer.class)
    private Boolean active;
}
