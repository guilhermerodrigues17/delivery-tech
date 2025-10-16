package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.mapper.RestaurantMapper;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.service.RestaurantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Validated
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
    public ResponseEntity<List<RestaurantResponseDto>> findAllActive() {
        var restaurant = restaurantService.findAllActive();
        var response = restaurant.stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}/delivery-tax", params = "cep")
    public ResponseEntity<Map<String, BigDecimal>> calculateDeliveryTax(
            @PathVariable String id,
            @RequestParam(required = true)
            @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Formato de CEP inválido. Use XXXXX-XXX ou XXXXXXXX.")
            String cep
    ) {
        BigDecimal deliveryTax = restaurantService.calculateDeliveryTax(id, cep);

        var response = new HashMap<String, BigDecimal>();
        response.put("deliveryTax", deliveryTax);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponseDto> updateRestaurant(@PathVariable String id,
                                                                  @Valid @RequestBody RestaurantRequestDto dto) {
        var response = restaurantService.updateRestaurant(id, dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateStatusActive(@PathVariable String id) {
        restaurantService.updateStatusActive(id);
        return ResponseEntity.noContent().build();
    }
}
