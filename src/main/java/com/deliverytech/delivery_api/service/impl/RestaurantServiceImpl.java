package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.request.RestaurantStatusUpdateDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.exceptions.CepZoneDistanceException;
import com.deliverytech.delivery_api.exceptions.DuplicatedRegisterException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.RestaurantMapper;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.model.enums.CepZonesDistance;
import com.deliverytech.delivery_api.repository.RestaurantRepository;
import com.deliverytech.delivery_api.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper mapper;

    public RestaurantResponseDto createRestaurant(RestaurantRequestDto dto) {
        if (existsByName(dto.getName())) {
            throw new DuplicatedRegisterException("Nome de restaurante já está em uso");

        }

        Restaurant restaurantEntity = mapper.toEntity(dto);
        restaurantEntity.setActive(true);

        Restaurant saved = restaurantRepository.save(restaurantEntity);
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Restaurant findById(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante não encontrado"));
    }

    @Transactional(readOnly = true)
    public RestaurantResponseDto findByIdResponse(String id) {
        Restaurant restaurantFound = findById(UUID.fromString(id));
        return mapper.toDto(restaurantFound);
    }

    public Boolean existsByName(String name) {
        return restaurantRepository.existsByName(name);
    }

    public Page<RestaurantResponseDto> searchRestaurants(String name, String category, String active, Pageable pageable) {
        var restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setCategory(category);
        restaurant.setActive(Boolean.parseBoolean(active));

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Restaurant> restaurantExample = Example.of(restaurant, matcher);

        Page<Restaurant> restaurants = restaurantRepository.findAll(restaurantExample, pageable);
        return restaurants.map(mapper::toDto);
    }

    public Page<RestaurantResponseDto> findAllActive(Pageable pageable) {
        Page<Restaurant> restaurantsPage = restaurantRepository.findByActiveTrue(pageable);
        return restaurantsPage.map(mapper::toDto);
    }

    public Page<RestaurantResponseDto> findRestaurantsNearby(String cep, Pageable pageable) {
        // TODO: add search by cep logic and integration

        return findAllActive(pageable);
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

    public void updateStatusActive(String id, RestaurantStatusUpdateDto dto) {
        Restaurant existingRestaurant = findById(UUID.fromString(id));
        existingRestaurant.setActive(dto.getActive());

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
