package com.deliverytech.delivery_api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.model.Restaurant;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "products", ignore = true)
    Restaurant toEntity(RestaurantRequestDto dto);

    RestaurantResponseDto toDto(Restaurant restaurant);
}
