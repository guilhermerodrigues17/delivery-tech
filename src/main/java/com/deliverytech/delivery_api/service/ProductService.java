package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto dto);
    ProductResponseDto findProductByIdResponse(String id);
    Product findProductEntityById(String id);
    Page<ProductResponseDto> findProductsByRestaurantId(String restaurantId, Pageable pageable);
    Page<ProductResponseDto> searchProducts(String name, String category, Pageable pageable);
    ProductResponseDto updateProduct(String id, ProductRequestDto dto);
    Product deleteProduct(String id);
    Product toggleAvailability(String id);
}
