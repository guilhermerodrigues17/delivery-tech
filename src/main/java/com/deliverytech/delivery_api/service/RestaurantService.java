package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.request.RestaurantStatusUpdateDto;
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
    List<RestaurantResponseDto> searchRestaurants(String name, String category, String active);
    List<Restaurant> findAllActive();
    List<Restaurant> findAll();
    List<RestaurantResponseDto> findRestaurantsNearby(String cep);
    RestaurantResponseDto updateRestaurant(String id, RestaurantRequestDto dto);
    void updateStatusActive(String id, RestaurantStatusUpdateDto dto);
    BigDecimal calculateDeliveryTax(String restaurantId, String cep);
}
