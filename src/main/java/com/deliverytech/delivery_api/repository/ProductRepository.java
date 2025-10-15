package com.deliverytech.delivery_api.repository;

import com.deliverytech.delivery_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByRestaurantId(UUID restaurantId);

    List<Product> findByRestaurantIdAndAvailableTrue(UUID restaurantId);

    List<Product> findByCategory(String category);

    List<Product> findByCategoryAndAvailableTrue(String category);

    List<Product> findByAvailableTrue();

    List<Product> findByPriceLessThanEqual(BigDecimal price);
}
