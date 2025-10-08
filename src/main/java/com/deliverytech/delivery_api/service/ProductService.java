package com.deliverytech.delivery_api.service;

import org.springframework.stereotype.Service;
import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.mapper.ProductMapper;
import com.deliverytech.delivery_api.model.Product;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RestaurantService restaurantService;
    private final ProductMapper productMapper;

    public Product createProduct(ProductRequestDto dto) {
        Restaurant restaurant = restaurantService.findById(dto.getRestaurantId());

        Product product = productMapper.toEntity(dto);
        product.setRestaurant(restaurant);

        return productRepository.save(product);
    }
}
