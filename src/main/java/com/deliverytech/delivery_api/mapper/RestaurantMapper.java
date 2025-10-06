package com.deliverytech.delivery_api.mapper;

import org.mapstruct.Mapper;
import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.model.Restaurant;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    Restaurant toEntity(RestaurantRequestDto dto);

    RestaurantResponseDto toDto(Restaurant restaurant);
}
