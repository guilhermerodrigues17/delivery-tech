package com.deliverytech.delivery_api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverytech.delivery_api.model.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    Optional<Restaurant> findByName(String name);

    Boolean existsByName(String name);

    List<Restaurant> findByCategory(String category);

    List<Restaurant> findByActiveTrue();

}
