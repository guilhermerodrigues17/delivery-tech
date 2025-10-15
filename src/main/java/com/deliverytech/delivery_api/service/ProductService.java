package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.exceptions.NotAllowedException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.ProductMapper;
import com.deliverytech.delivery_api.model.Product;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

    @Transactional(readOnly = true)
    public Product findProductEntityById(String id) {
        return productRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findProductsByRestaurantIdResponse(String restaurantId) {
        var restaurant = restaurantService.findById(UUID.fromString(restaurantId));

        var products = productRepository.findByRestaurantId(restaurant.getId());

        return products.stream().map(productMapper::toResponseDto).toList();
    }

    @Transactional
    public ProductResponseDto updateProduct(String id, ProductRequestDto dto) {
        var product = findProductEntityById(id);
        if (!product.getRestaurant().getId().equals(dto.getRestaurantId())) {
            throw new NotAllowedException("Produto não pertence ao restaurante informado");
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setAvailable(dto.getAvailable());

        var response = productRepository.save(product);

        return productMapper.toResponseDto(response);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(String id) {
        var product = findProductEntityById(id);
        productRepository.delete(product);
    }
}
