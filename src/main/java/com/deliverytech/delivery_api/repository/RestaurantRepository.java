package com.deliverytech.delivery_api.repository;

import com.deliverytech.delivery_api.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    Optional<Restaurant> findByName(String name);

    Boolean existsByName(String name);

    List<Restaurant> findByCategory(String category);

    Page<Restaurant> findByActiveTrue(Pageable pageable);

    List<Restaurant> findByDeliveryTaxLessThanEqual(BigDecimal deliveryTax);

    List<Restaurant> findTop5ByOrderByNameAsc();
}
