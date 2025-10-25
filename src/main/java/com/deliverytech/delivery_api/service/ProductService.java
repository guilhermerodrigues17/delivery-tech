package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductRequestDto dto);
    ProductResponseDto findProductByIdResponse(String id);
    Product findProductEntityById(String id);
    List<ProductResponseDto> findProductsByRestaurantIdResponse(String restaurantId);
    Page<ProductResponseDto> searchProducts(String name, String category, Pageable pageable);
    ProductResponseDto updateProduct(String id, ProductRequestDto dto);
    void deleteProduct(String id);
    void toggleAvailability(String id);
}
