package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.model.Restaurant;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RestaurantService {
    Restaurant createRestaurant(Restaurant restaurant);
    Restaurant findById(UUID id);
    Restaurant findByName(String name);
    Boolean existsByName(String name);
    List<RestaurantResponseDto> findByCategory(String category);
    List<Restaurant> searchRestaurants(String name, String category);
    List<Restaurant> findAllActive();
    List<Restaurant> findAll();
    RestaurantResponseDto updateRestaurant(String id, RestaurantRequestDto dto);
    void updateStatusActive(String id);
    BigDecimal calculateDeliveryTax(String restaurantId, String cep);
}
