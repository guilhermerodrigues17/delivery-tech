package com.deliverytech.delivery_api.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.mapper.RestaurantMapper;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final RestaurantMapper mapper;

    @PostMapping
    public ResponseEntity<RestaurantResponseDto> createRestaurant(
            @Valid @RequestBody RestaurantRequestDto dto) {
        var restaurantEntity = mapper.toEntity(dto);
        Restaurant restaurant = restaurantService.createRestaurant(restaurantEntity);
        var response = mapper.toDto(restaurant);
        return ResponseEntity.created(null).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDto> findById(@PathVariable String id) {
        var uuid = java.util.UUID.fromString(id);
        Restaurant restaurant = restaurantService.findById(uuid);
        var response = mapper.toDto(restaurant);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponseDto>> searchRestaurants(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        var restaurants = restaurantService.searchRestaurants(name, category);
        var response = restaurants.stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponseDto>> findAll() {
        var restaurant = restaurantService.findAll();
        var response = restaurant.stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(response);
    }
}
