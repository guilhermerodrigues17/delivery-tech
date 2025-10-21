package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.request.RestaurantStatusUpdateDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.mapper.RestaurantMapper;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.service.ProductService;
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
    private final ProductService productService;
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
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "true") String active) {
        var restaurants = restaurantService.searchRestaurants(name, category, active);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponseDto>> findAllActive() {
        var restaurant = restaurantService.findAllActive();
        var response = restaurant.stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping(params = "category")
    public ResponseEntity<List<RestaurantResponseDto>> findByCategory(@RequestParam String category) {
        var response = restaurantService.findByCategory(category);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{restaurantId}/products")
    public ResponseEntity<List<ProductResponseDto>> findProductsByRestaurantId(@PathVariable String restaurantId) {
        var response = productService.findProductsByRestaurantIdResponse(restaurantId);
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

    @GetMapping(value = "/nearby", params = "cep")
    public ResponseEntity<List<RestaurantResponseDto>> findRestaurantsNearby(
            @RequestParam
            @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Formato de CEP inválido. Use XXXXX-XXX ou XXXXXXXX.")
            String cep) {
        var restaurants = restaurantService.findRestaurantsNearby(cep);
        return ResponseEntity.ok(restaurants);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponseDto> updateRestaurant(@PathVariable String id,
                                                                  @Valid @RequestBody RestaurantRequestDto dto) {
        var response = restaurantService.updateRestaurant(id, dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatusActive(@PathVariable String id,
                                                   @Valid @RequestBody RestaurantStatusUpdateDto dto) {
        restaurantService.updateStatusActive(id, dto);
        return ResponseEntity.noContent().build();
    }
}
