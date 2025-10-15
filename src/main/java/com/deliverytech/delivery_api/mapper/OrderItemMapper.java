package com.deliverytech.delivery_api.mapper;

import com.deliverytech.delivery_api.dto.response.OrderItemResponseDto;
import com.deliverytech.delivery_api.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mappings({@Mapping(target = "productId", source = "product.id"),
            @Mapping(target = "productName", source = "product.name")})
    OrderItemResponseDto toDto(OrderItem orderItem);
}
