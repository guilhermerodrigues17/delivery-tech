package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.request.RestaurantStatusUpdateDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.events.restaurant.RestaurantCreatedEvent;
import com.deliverytech.delivery_api.events.restaurant.RestaurantDisableEvent;
import com.deliverytech.delivery_api.events.restaurant.RestaurantUpdateEvent;
import com.deliverytech.delivery_api.exceptions.BusinessException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.RestaurantMapper;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.model.enums.CepZonesDistance;
import com.deliverytech.delivery_api.repository.RestaurantRepository;
import com.deliverytech.delivery_api.security.SecurityService;
import com.deliverytech.delivery_api.service.RestaurantService;
import com.deliverytech.delivery_api.validation.RestaurantValidator;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service("restaurantServiceImpl")
@RequiredArgsConstructor
@Transactional
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper mapper;
    private final SecurityService securityService;
    private final RestaurantValidator restaurantValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Timed("delivery_api.restaurants.creation.timer")
    @CacheEvict(value = "restaurants", allEntries = true)
    public RestaurantResponseDto createRestaurant(RestaurantRequestDto dto) {
        restaurantValidator.validateName(dto.getName());

        Restaurant restaurantEntity = mapper.toEntity(dto);
        restaurantEntity.setActive(true);

        String rawPhone = dto.getPhoneNumber();
        if (rawPhone != null) {
            var trimmedPhone = rawPhone.replaceAll("\\D", "");
            restaurantEntity.setPhoneNumber(trimmedPhone);
        }

        Restaurant saved = restaurantRepository.save(restaurantEntity);

        var currentUserOpt = securityService.getCurrentUser();
        String currentUser = "ANONYMOUS";
        if (currentUserOpt.isPresent()) {
            currentUser = currentUserOpt.get().getEmail();
        }
        eventPublisher.publishEvent(new RestaurantCreatedEvent(this, saved, currentUser));

        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Restaurant findById(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante não encontrado"));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "restaurants", key = "#id")
    public RestaurantResponseDto findByIdResponse(String id) {
        Restaurant restaurantFound = findById(UUID.fromString(id));
        return mapper.toDto(restaurantFound);
    }

    public Boolean existsByName(String name) {
        return restaurantRepository.existsByName(name);
    }

    @Timed("delivery_api.restaurants.search.timer")
    public Page<RestaurantResponseDto> searchRestaurants(String name, String category, Boolean active, Pageable pageable) {
        var restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setCategory(category);
        restaurant.setActive(active);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Restaurant> restaurantExample = Example.of(restaurant, matcher);

        Page<Restaurant> restaurants = restaurantRepository.findAll(restaurantExample, pageable);
        return restaurants.map(mapper::toDto);
    }

    @Cacheable(value = "restaurants", key = "'allActive'")
    public Page<RestaurantResponseDto> findAllActive(Pageable pageable) {
        Page<Restaurant> restaurantsPage = restaurantRepository.findByActiveTrue(pageable);
        return restaurantsPage.map(mapper::toDto);
    }

    public Page<RestaurantResponseDto> findRestaurantsNearby(String cep, Pageable pageable) {
        // TODO: add search by cep logic and integration

        return findAllActive(pageable);
    }

    @Caching(evict = {
            @CacheEvict(value = "restaurants", key = "#id"),
            @CacheEvict(value = "restaurants", key = "'allActive'")
    })
    public RestaurantResponseDto updateRestaurant(String id, RestaurantRequestDto dto) {
        Restaurant existingRestaurant = findById(UUID.fromString(id));

        if (!existingRestaurant.getName().equals(dto.getName())) {
            restaurantValidator.validateName(dto.getName());
            existingRestaurant.setName(dto.getName());
        }

        existingRestaurant.setCategory(dto.getCategory());
        existingRestaurant.setAddress(dto.getAddress());
        existingRestaurant.setDeliveryTax(dto.getDeliveryTax());

        String rawPhone = dto.getPhoneNumber();
        if (rawPhone != null) {
            var trimmedPhone = rawPhone.replaceAll("\\D", "");
            existingRestaurant.setPhoneNumber(trimmedPhone);
        }

        var updatedRestaurant = restaurantRepository.save(existingRestaurant);

        var currentUserOpt = securityService.getCurrentUser();
        String currentUser = "ANONYMOUS";
        if (currentUserOpt.isPresent()) {
            currentUser = currentUserOpt.get().getEmail();
        }
        eventPublisher.publishEvent(new RestaurantUpdateEvent(this, updatedRestaurant, currentUser));

        return mapper.toDto(updatedRestaurant);
    }

    @Caching(evict = {
            @CacheEvict(value = "restaurants", key = "#id"),
            @CacheEvict(value = "restaurants", key = "'allActive'")
    })
    public void updateStatusActive(String id, RestaurantStatusUpdateDto dto) {
        Restaurant existingRestaurant = findById(UUID.fromString(id));
        existingRestaurant.setActive(dto.getActive());

        restaurantRepository.save(existingRestaurant);
        var currentUserOpt = securityService.getCurrentUser();
        String currentUser = "ANONYMOUS";
        if (currentUserOpt.isPresent()) {
            currentUser = currentUserOpt.get().getEmail();
        }
        eventPublisher.publishEvent(new RestaurantDisableEvent(this, existingRestaurant, currentUser));
    }

    public BigDecimal calculateDeliveryTax(String restaurantId, String cep) {
        Restaurant restaurant = findById(UUID.fromString(restaurantId));
        var deliveryTaxBase = restaurant.getDeliveryTax();

        String numericCep = cep.replaceAll("\\D", "");
        CepZonesDistance cepZoneDistance = CepZonesDistance.getCepZoneDistance(numericCep)
                .orElseThrow(() -> new BusinessException(
                        "Desculpe, este restaurante não realiza entregas para o CEP informado."));

        return switch (cepZoneDistance) {
            case SHORT_DISTANCE -> deliveryTaxBase;
            case MEDIUM_DISTANCE -> deliveryTaxBase.add(new BigDecimal("5.00"));
            case LONG_DISTANCE -> deliveryTaxBase.add(new BigDecimal("10.00"));
        };
    }

    public boolean isOwner(String restaurantId) {
        Optional<UUID> currentUserRestaurantId = securityService.getCurrentUserRestaurantId();
        return currentUserRestaurantId.filter(uuid -> UUID.fromString(restaurantId).equals(uuid)).isPresent();
    }

}
