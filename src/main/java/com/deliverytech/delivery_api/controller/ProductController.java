package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.mapper.ProductMapper;
import com.deliverytech.delivery_api.model.Product;
import com.deliverytech.delivery_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductRequestDto dto) {
        Product product = productService.createProduct(dto);
        var response = productMapper.toResponseDto(product);

        return ResponseEntity.created(null).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> findProductById(@PathVariable String id) {
        var response = productService.findProductByIdResponse(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping(params = "restaurantId")
    public ResponseEntity<List<ProductResponseDto>> findProductsByRestaurantId(
            @RequestParam String restaurantId) {
        var response = productService.findProductsByRestaurantIdResponse(restaurantId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable String id,
                                                            @Valid @RequestBody ProductRequestDto dto) {
        var response = productService.updateProduct(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
