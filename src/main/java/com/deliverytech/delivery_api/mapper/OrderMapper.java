package com.deliverytech.delivery_api.mapper;

import com.deliverytech.delivery_api.dto.response.OrderResponseDto;
import com.deliverytech.delivery_api.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "consumerName", source = "consumer.name")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    OrderResponseDto toDto(Order order);
}
