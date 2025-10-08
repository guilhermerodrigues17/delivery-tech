package com.deliverytech.delivery_api.service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverytech.delivery_api.exceptions.DuplicatedRegisterException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public Restaurant createRestaurant(Restaurant restaurant) {
        var isNameTaken = restaurantRepository.findByName(restaurant.getName()).orElse(null);
        if (isNameTaken != null) {
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

    @Transactional(readOnly = true)
    public List<Restaurant> findByCategory(String category) {
        return restaurantRepository.findByCategory(category);
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
}
