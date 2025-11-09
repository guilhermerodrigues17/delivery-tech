package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.request.RestaurantStatusUpdateDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.exceptions.BusinessException;
import com.deliverytech.delivery_api.exceptions.ConflictException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.RestaurantMapper;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.model.enums.CepZonesDistance;
import com.deliverytech.delivery_api.repository.RestaurantRepository;
import com.deliverytech.delivery_api.security.SecurityService;
import com.deliverytech.delivery_api.service.RestaurantService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Timed("delivery_api.restaurants.creation.timer")
    public RestaurantResponseDto createRestaurant(RestaurantRequestDto dto) {
        if (existsByName(dto.getName())) {
            throw new ConflictException("Nome de restaurante já está em uso");
        }

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

        auditLogger.info("CRUD_EVENT; type=CREATE; entity=Restaurant; entityId={}; user={}; correlationId={}",
                saved.getId(),
                currentUser,
                MDC.get("correlationId")
        );


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
                throw new ConflictException("Nome de restaurante já está em uso");
            }
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

        auditLogger.info("CRUD_EVENT; type=UPDATE; entity=Restaurant; entityId={}; user={}; correlationId={}",
                updatedRestaurant.getId(),
                currentUser,
                MDC.get("correlationId")
        );

        return mapper.toDto(updatedRestaurant);
    }

    public void updateStatusActive(String id, RestaurantStatusUpdateDto dto) {
        Restaurant existingRestaurant = findById(UUID.fromString(id));
        existingRestaurant.setActive(dto.getActive());

        restaurantRepository.save(existingRestaurant);
        var currentUserOpt = securityService.getCurrentUser();
        String currentUser = "ANONYMOUS";
        if (currentUserOpt.isPresent()) {
            currentUser = currentUserOpt.get().getEmail();
        }

        auditLogger.info("CRUD_EVENT; type=DELETE; entity=Restaurant; entityId={}; user={}; correlationId={}",
                existingRestaurant.getId(),
                currentUser,
                MDC.get("correlationId")
        );
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
