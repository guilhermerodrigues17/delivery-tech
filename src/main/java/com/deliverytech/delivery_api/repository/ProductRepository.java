package com.deliverytech.delivery_api.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverytech.delivery_api.model.Product;
import java.math.BigDecimal;
import java.util.List;


public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByRestaurantId(UUID restaurantId);

    List<Product> findByRestaurantIdAndAvailableTrue(UUID restaurantId);

    List<Product> findByCategory(String category);

    List<Product> findByCategoryAndAvailableTrue(String category);

    List<Product> findByAvailableTrue();

    List<Product> findByPriceLessThanEqual(BigDecimal price);
}
