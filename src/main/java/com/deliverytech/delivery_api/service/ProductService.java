package com.deliverytech.delivery_api.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.ProductMapper;
import com.deliverytech.delivery_api.model.Product;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
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

    @Transactional(readOnly = true)
    public ProductResponseDto findProductByIdResponse(String id) {
        var product = findProductEntityById(id);
        return productMapper.toResponseDto(product);
    }

    public Product findProductEntityById(String id) {
        return productRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }
}
