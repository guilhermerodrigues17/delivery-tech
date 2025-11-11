package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.events.product.ProductCreateEvent;
import com.deliverytech.delivery_api.events.product.ProductDeleteEvent;
import com.deliverytech.delivery_api.events.product.ProductUpdateEvent;
import com.deliverytech.delivery_api.exceptions.BusinessException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.ProductMapper;
import com.deliverytech.delivery_api.model.Product;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.repository.ProductRepository;
import com.deliverytech.delivery_api.security.SecurityService;
import com.deliverytech.delivery_api.service.ProductService;
import com.deliverytech.delivery_api.service.RestaurantService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service("productServiceImpl")
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final RestaurantService restaurantService;
    private final ProductMapper productMapper;
    private final SecurityService securityService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Timed("delivery_api.products.creation.timer")
    @CacheEvict(value = "products_by_restaurant", key = "#dto.restaurantId")
    public ProductResponseDto createProduct(ProductRequestDto dto) {
        Restaurant restaurant = restaurantService.findById(dto.getRestaurantId());

        Product product = productMapper.toEntity(dto);
        product.setRestaurant(restaurant);

        var savedProduct = productRepository.save(product);

        var currentUserOpt = securityService.getCurrentUser();
        String currentUser = "ANONYMOUS";
        if (currentUserOpt.isPresent()) {
            currentUser = currentUserOpt.get().getEmail();
        }
        eventPublisher.publishEvent(new ProductCreateEvent(this, savedProduct, currentUser));

        return productMapper.toResponseDto(savedProduct);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
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
    @Timed("delivery_api.products.findProductsByRestaurantId.timer")
    @Cacheable(value = "products_by_restaurant", key = "#restaurantId")
    public Page<ProductResponseDto> findProductsByRestaurantId(String restaurantId, Pageable pageable) {
        var restaurant = restaurantService.findById(UUID.fromString(restaurantId));

        var productsPage = productRepository.findByRestaurantId(restaurant.getId(), pageable);
        return productsPage.map(productMapper::toResponseDto);
    }

    @Override
    public Page<ProductResponseDto> searchProducts(String name, String category, Pageable pageable) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setAvailable(true);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Product> productExample = Example.of(product, matcher);

        Page<Product> productsPage = productRepository.findAll(productExample, pageable);
        return productsPage.map(productMapper::toResponseDto);
    }

    @Transactional
    @Timed("delivery_api.products.update.timer")
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products_by_restaurant", key = "#dto.restaurantId")
    })
    public ProductResponseDto updateProduct(String id, ProductRequestDto dto) {
        var product = findProductEntityById(id);
        if (!product.getRestaurant().getId().equals(dto.getRestaurantId())) {
            throw new BusinessException(
                    String.format("O produto '%s' (%s) não pertence ao restaurante informado.",
                            product.getName(), product.getId()));
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setAvailable(dto.getAvailable());

        var updatedProduct = productRepository.save(product);

        var currentUserOpt = securityService.getCurrentUser();
        String currentUser = "ANONYMOUS";
        if (currentUserOpt.isPresent()) {
            currentUser = currentUserOpt.get().getEmail();
        }
        eventPublisher.publishEvent(new ProductUpdateEvent(this, updatedProduct, currentUser));

        return productMapper.toResponseDto(updatedProduct);
    }

    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products_by_restaurant", key = "#result.restaurant.id")
    })
    public Product deleteProduct(String id) {
        var productToDelete = findProductEntityById(id);
        productRepository.delete(productToDelete);

        var currentUserOpt = securityService.getCurrentUser();
        String currentUser = "ANONYMOUS";
        if (currentUserOpt.isPresent()) {
            currentUser = currentUserOpt.get().getEmail();
        }
        eventPublisher.publishEvent(new ProductDeleteEvent(this, productToDelete, currentUser));

        return productToDelete;
    }

    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products_by_restaurant", key = "#result.restaurant.id")
    })
    public Product toggleAvailability(String id) {
        Product productFound = findProductEntityById(id);
        productFound.setAvailable(!productFound.getAvailable());

        return productRepository.save(productFound);
    }

    public boolean isOwnerOfProductRestaurant(String productId) {
        Optional<UUID> currentUserRestaurantId = securityService.getCurrentUserRestaurantId();
        if (currentUserRestaurantId.isEmpty()) {
            return false;
        }

        Product product = findProductEntityById(productId);
        return product.getRestaurant().getId().equals(currentUserRestaurantId.get());
    }
}
