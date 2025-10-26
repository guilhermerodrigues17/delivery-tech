package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.request.RestaurantStatusUpdateDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RestaurantService {
    RestaurantResponseDto createRestaurant(RestaurantRequestDto dto);
    Restaurant findById(UUID id);
    RestaurantResponseDto findByIdResponse(String id);
    Boolean existsByName(String name);
    List<RestaurantResponseDto> findByCategory(String category);
    Page<RestaurantResponseDto> searchRestaurants(String name, String category, String active, Pageable pageable);
    Page<RestaurantResponseDto> findAllActive(Pageable pageable);
    List<RestaurantResponseDto> findRestaurantsNearby(String cep);
    RestaurantResponseDto updateRestaurant(String id, RestaurantRequestDto dto);
    void updateStatusActive(String id, RestaurantStatusUpdateDto dto);
    BigDecimal calculateDeliveryTax(String restaurantId, String cep);
}
