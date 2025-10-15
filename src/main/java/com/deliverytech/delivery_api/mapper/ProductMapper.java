package com.deliverytech.delivery_api.mapper;

import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "restaurant.id", source = "restaurantId")
    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequestDto dto);

    @Mapping(target = "restaurantName", source = "restaurant.name")
    @Mapping(target = "restaurantId", source = "restaurant.id")
    ProductResponseDto toResponseDto(Product product);
}
