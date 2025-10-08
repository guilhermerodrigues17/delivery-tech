package com.deliverytech.delivery_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.deliverytech.delivery_api.dto.response.OrderResponseDto;
import com.deliverytech.delivery_api.model.Order;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "consumerName", source = "consumer.name")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    OrderResponseDto toDto(Order order);
}
