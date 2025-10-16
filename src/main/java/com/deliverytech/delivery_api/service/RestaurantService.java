package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.exceptions.CepZoneDistanceException;
import com.deliverytech.delivery_api.exceptions.DuplicatedRegisterException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.RestaurantMapper;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.model.enums.CepZonesDistance;
import com.deliverytech.delivery_api.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper mapper;

    public Restaurant createRestaurant(Restaurant restaurant) {
        if (existsByName(restaurant.getName())) {
            throw new DuplicatedRegisterException("Nome de restaurante já está em uso");

        }

        restaurant.setActive(true);
        return restaurantRepository.save(restaurant);
    }

    @Transactional(readOnly = true)
    public Restaurant findById(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante não encontrado"));
    }

    @Transactional(readOnly = true)
    public Restaurant findByName(String name) {
        return restaurantRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante não encontrado"));
    }

    public Boolean existsByName(String name) {
        return restaurantRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponseDto> findByCategory(String category) {
        var restaurants = restaurantRepository.findByCategory(category);
        return restaurants.stream().map(mapper::toDto).toList();
    }

    public List<Restaurant> searchRestaurants(String name, String category) {
        var restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setCategory(category);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Restaurant> example = Example.of(restaurant, matcher);

        return restaurantRepository.findAll(example);
    }

    public List<Restaurant> findAllActive() {
        return restaurantRepository.findByActiveTrue();
    }

    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    public RestaurantResponseDto updateRestaurant(String id, RestaurantRequestDto dto) {
        Restaurant existingRestaurant = findById(UUID.fromString(id));

        if (!existingRestaurant.getName().equals(dto.getName())) {
            if (existsByName(dto.getName())) {
                throw new DuplicatedRegisterException("Nome de restaurante já está em uso");
            }
            existingRestaurant.setName(dto.getName());
        }

        existingRestaurant.setCategory(dto.getCategory());
        existingRestaurant.setAddress(dto.getAddress());
        existingRestaurant.setPhoneNumber(dto.getPhoneNumber());
        existingRestaurant.setDeliveryTax(dto.getDeliveryTax());

        var updatedRestaurant = restaurantRepository.save(existingRestaurant);

        return mapper.toDto(updatedRestaurant);
    }

    public void updateStatusActive(String id) {
        Restaurant existingRestaurant = findById(UUID.fromString(id));
        existingRestaurant.setActive(!existingRestaurant.getActive());

        restaurantRepository.save(existingRestaurant);
    }

    public BigDecimal calculateDeliveryTax(String restaurantId, String cep) {
        Restaurant restaurant = findById(UUID.fromString(restaurantId));
        var deliveryTaxBase = restaurant.getDeliveryTax();

        String numericCep = cep.replaceAll("\\D", "");
        CepZonesDistance cepZoneDistance = CepZonesDistance.getCepZoneDistance(numericCep)
                .orElseThrow(() -> new CepZoneDistanceException(
                        "Desculpe, este restaurante não realiza entregas para o CEP informado."));

        return switch (cepZoneDistance) {
            case SHORT_DISTANCE -> deliveryTaxBase;
            case MEDIUM_DISTANCE -> deliveryTaxBase.add(new BigDecimal("5.00"));
            case LONG_DISTANCE -> deliveryTaxBase.add(new BigDecimal("10.00"));
        };
    }

}
