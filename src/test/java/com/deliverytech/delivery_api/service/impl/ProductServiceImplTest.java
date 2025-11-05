package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.exceptions.BusinessException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.ProductMapper;
import com.deliverytech.delivery_api.model.Product;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.repository.ProductRepository;
import com.deliverytech.delivery_api.security.SecurityService;
import com.deliverytech.delivery_api.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private ProductServiceImpl productServiceImpl;

    @Nested
    @DisplayName("createProduct() tests")
    class CreateProductTests {


        @Test
        @DisplayName("Should throw ResourceNotFoundException when Restaurant ID does not exist")
        void should_ThrowResourceNotFoundException_When_RestaurantIdDoesNotExist() {
            ProductRequestDto dto = new ProductRequestDto();
            dto.setRestaurantId(UUID.randomUUID());
            dto.setName("Test Product");

            when(restaurantService.findById(dto.getRestaurantId())).thenThrow(new ResourceNotFoundException("Restaurante não encontrado"));

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                productServiceImpl.createProduct(dto);
            });

            assertEquals("Restaurante não encontrado", exception.getMessage());

            verify(restaurantService).findById(dto.getRestaurantId());
            verify(productMapper, never()).toEntity(any(ProductRequestDto.class));
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should create product and set restaurant when data is valid")
        void should_CreateProduct_When_DataIsValid() {
            ProductRequestDto dto = new ProductRequestDto();
            dto.setRestaurantId(UUID.randomUUID());
            dto.setName("New Pizza");
            dto.setPrice(BigDecimal.TEN);
            dto.setCategory("PIZZA");
            dto.setAvailable(true);

            Restaurant foundRestaurant = new Restaurant();
            foundRestaurant.setId(dto.getRestaurantId());
            foundRestaurant.setName("Test Pizzaria");

            Product productFromMapper = new Product();
            productFromMapper.setName(dto.getName());

            Product savedProduct = new Product();
            savedProduct.setId(UUID.randomUUID());
            savedProduct.setName(dto.getName());
            savedProduct.setRestaurant(foundRestaurant);

            ProductResponseDto expectedDto = new ProductResponseDto(
                    savedProduct.getId(),
                    savedProduct.getName(),
                    null,
                    dto.getCategory(),
                    true,
                    BigDecimal.TEN,
                    savedProduct.getRestaurant().getName(),
                    dto.getRestaurantId()
            );

            when(restaurantService.findById(dto.getRestaurantId())).thenReturn(foundRestaurant);
            when(productMapper.toEntity(dto)).thenReturn(productFromMapper);
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
            when(productMapper.toResponseDto(savedProduct)).thenReturn(expectedDto);

            ProductResponseDto actualDto = productServiceImpl.createProduct(dto);

            assertNotNull(actualDto);

            assertEquals(expectedDto.id(), actualDto.id());
            assertEquals(expectedDto.name(), actualDto.name());
            assertEquals(expectedDto.restaurantName(), actualDto.restaurantName());

            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(productCaptor.capture());

            Product capturedProduct = productCaptor.getValue();
            assertEquals(foundRestaurant, capturedProduct.getRestaurant());

            verify(restaurantService).findById(dto.getRestaurantId());
            verify(productMapper).toEntity(dto);
            verify(productRepository).save(productFromMapper);
            verify(productMapper).toResponseDto(savedProduct);
        }
    }

    @Nested
    @DisplayName("updateProduct() tests")
    class UpdateProductTests {

        private ProductRequestDto updateDto;
        private Product existingProduct;
        private Restaurant mockRestaurant;
        private UUID productId;
        private UUID restaurantId;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();
            restaurantId = UUID.randomUUID();

            updateDto = new ProductRequestDto();
            updateDto.setRestaurantId(restaurantId);
            updateDto.setName("Updated Pizza");
            updateDto.setDescription("Updated Description");
            updateDto.setPrice(new BigDecimal("45.00"));
            updateDto.setCategory("PIZZA");
            updateDto.setAvailable(true);

            mockRestaurant = new Restaurant();
            mockRestaurant.setId(restaurantId);
            mockRestaurant.setName("Test Pizzaria");

            existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setName("Old Pizza");
            existingProduct.setDescription("Old Description");
            existingProduct.setPrice(new BigDecimal("30.00"));
            existingProduct.setCategory("Massas");
            existingProduct.setAvailable(false);
            existingProduct.setRestaurant(mockRestaurant);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product to update does not exist")
        void should_ThrowResourceNotFound_When_ProductNotFound() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                productServiceImpl.updateProduct(productId.toString(), updateDto);
            });

            verify(productRepository).findById(productId);
            verify(productRepository, never()).save(any(Product.class));
            verify(productMapper, never()).toResponseDto(any(Product.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when RestaurantId in DTO does not match product's owner")
        void should_ThrowBusinessException_When_RestaurantIdDoesNotMatch() {
            updateDto.setRestaurantId(UUID.randomUUID());

            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                productServiceImpl.updateProduct(productId.toString(), updateDto);
            });

            String expectedMessage = String.format("O produto '%s' (%s) não pertence ao restaurante informado.",
                    existingProduct.getName(), existingProduct.getId());
            assertEquals(expectedMessage, exception.getMessage());

            verify(productRepository).findById(productId);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should update all fields correctly when data is valid and restaurant matches")
        void should_UpdateProduct_When_DataIsValidAndRestaurantMatches() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
            when(productMapper.toResponseDto(any(Product.class))).thenAnswer(inv -> {
                Product mutated = inv.getArgument(0);
                return new ProductResponseDto(
                        mutated.getId(), mutated.getName(), mutated.getDescription(),
                        mutated.getCategory(), mutated.getAvailable(), mutated.getPrice(),
                        mutated.getRestaurant().getName(), mutated.getRestaurant().getId()
                );
            });

            ProductResponseDto responseDto = productServiceImpl.updateProduct(productId.toString(), updateDto);

            assertNotNull(responseDto);
            assertEquals(updateDto.getName(), responseDto.name());
            assertEquals(updateDto.getPrice(), responseDto.price());
            assertEquals(updateDto.getCategory(), responseDto.category());
            assertEquals(updateDto.getAvailable(), responseDto.available());

            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(productCaptor.capture());

            Product capturedProduct = productCaptor.getValue();
            assertEquals(productId, capturedProduct.getId());
            assertEquals("Updated Pizza", capturedProduct.getName());
            assertEquals("Updated Description", capturedProduct.getDescription());
            assertEquals(new BigDecimal("45.00"), capturedProduct.getPrice());

            verify(productRepository).findById(productId);
            verify(productRepository).save(existingProduct);
            verify(productMapper).toResponseDto(existingProduct);
        }
    }

    @Nested
    @DisplayName("deleteProduct() tests")
    class DeleteProductTests {

        private UUID productId;
        private Product existingProduct;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();

            existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setName("Product to delete");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product to delete does not exist")
        void should_ThrowResourceNotFound_When_ProductNotFound() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                productServiceImpl.deleteProduct(productId.toString());
            });

            verify(productRepository).findById(productId);
            verify(productRepository, never()).delete(any(Product.class));
        }

        @Test
        @DisplayName("Should call repository.delete when product is found")
        void should_CallDelete_When_ProductIsFound() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
            doNothing().when(productRepository).delete(any(Product.class));

            assertDoesNotThrow(() -> {
                productServiceImpl.deleteProduct(productId.toString());
            });

            verify(productRepository).findById(productId);
            verify(productRepository).delete(existingProduct);
        }
    }

    @Nested
    @DisplayName("toggleAvailability() tests")
    class ToggleAvailabilityTests {

        private UUID productId;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product does not exist")
        void should_ThrowResourceNotFound_When_ProductNotFound() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                productServiceImpl.toggleAvailability(productId.toString());
            });

            verify(productRepository).findById(productId);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should toggle availability from true to false")
        void should_ToggleAvailability_FromTrueToFalse() {
            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setAvailable(true);

            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(null);

            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

            assertDoesNotThrow(() -> {
                productServiceImpl.toggleAvailability(productId.toString());
            });

            verify(productRepository).findById(productId);
            verify(productRepository).save(productCaptor.capture());

            Product savedProduct = productCaptor.getValue();
            assertFalse(savedProduct.getAvailable(), "Product availability should be toggled to false");
        }

        @Test
        @DisplayName("Should toggle availability from false to true")
        void should_ToggleAvailability_FromFalseToTrue() {
            Product existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setAvailable(false);

            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any(Product.class))).thenReturn(null);

            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

            assertDoesNotThrow(() -> {
                productServiceImpl.toggleAvailability(productId.toString());
            });

            verify(productRepository).findById(productId);
            verify(productRepository).save(productCaptor.capture());

            Product savedProduct = productCaptor.getValue();
            assertTrue(savedProduct.getAvailable(), "Product availability should be toggled to true");
        }
    }

    @Nested
    @DisplayName("findProductByIdResponse() tests")
    class FindProductByIdResponseTests {

        private UUID productId;
        private String productIdString;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();
            productIdString = productId.toString();
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when ID is not a valid UUID")
        void should_ThrowIllegalArgumentException_When_IdIsInvalidUUID() {
            String invalidUuidString = "not-a-real-uuid";

            assertThrows(IllegalArgumentException.class, () -> {
                productServiceImpl.findProductByIdResponse(invalidUuidString);
            });

            verify(productRepository, never()).findById(any(UUID.class));
            verify(productMapper, never()).toResponseDto(any(Product.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product ID does not exist")
        void should_ThrowResourceNotFound_When_IdDoesNotExist() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                productServiceImpl.findProductByIdResponse(productIdString);
            });

            assertEquals("Produto não encontrado", exception.getMessage());

            verify(productRepository).findById(productId);
            verify(productMapper, never()).toResponseDto(any(Product.class));
        }

        @Test
        @DisplayName("Should return ProductResponseDto when ID exists")
        void should_ReturnDto_When_IdExists() {
            Product foundProduct = new Product();
            foundProduct.setId(productId);
            foundProduct.setName("Found Product");
            foundProduct.setPrice(BigDecimal.TEN);
            foundProduct.setRestaurant(new Restaurant());

            ProductResponseDto expectedDto = new ProductResponseDto(
                    productId, "Found Product", null, null, null, BigDecimal.TEN, null, null
            );

            when(productRepository.findById(productId)).thenReturn(Optional.of(foundProduct));
            when(productMapper.toResponseDto(foundProduct)).thenReturn(expectedDto);

            ProductResponseDto actualDto = productServiceImpl.findProductByIdResponse(productIdString);

            assertNotNull(actualDto);
            assertEquals(expectedDto, actualDto);
            assertEquals(productIdString, actualDto.id().toString());

            verify(productRepository).findById(productId);
            verify(productMapper).toResponseDto(foundProduct);
        }
    }

    @Nested
    @DisplayName("findProductsByRestaurantId() tests")
    class FindProductsByRestaurantIdTests {

        private UUID restaurantId;
        private String restaurantIdString;
        private Pageable pageable;
        private Restaurant mockRestaurant;

        @BeforeEach
        void setUp() {
            restaurantId = UUID.randomUUID();
            restaurantIdString = restaurantId.toString();
            pageable = PageRequest.of(0, 5);

            mockRestaurant = new Restaurant();
            mockRestaurant.setId(restaurantId);
            mockRestaurant.setName("Test Restaurant");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when restaurantId is not a valid UUID")
        void should_ThrowIllegalArgumentException_When_IdIsInvalidUUID() {
            String invalidUuidString = "not-a-uuid";

            assertThrows(IllegalArgumentException.class, () -> {
                productServiceImpl.findProductsByRestaurantId(invalidUuidString, pageable);
            });

            verify(restaurantService, never()).findById(any(UUID.class));
            verify(productRepository, never()).findByRestaurantId(any(UUID.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when restaurantId does not exist")
        void should_ThrowResourceNotFound_When_RestaurantNotFound() {
            when(restaurantService.findById(restaurantId))
                    .thenThrow(new ResourceNotFoundException("Restaurante não encontrado"));

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                productServiceImpl.findProductsByRestaurantId(restaurantIdString, pageable);
            });

            assertEquals("Restaurante não encontrado", exception.getMessage());

            verify(restaurantService).findById(restaurantId);
            verify(productRepository, never()).findByRestaurantId(any(UUID.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty Page when restaurant has no products")
        void should_ReturnEmptyPage_When_RestaurantHasNoProducts() {
            when(restaurantService.findById(restaurantId)).thenReturn(mockRestaurant);

            Page<Product> emptyPage = Page.empty(pageable);
            when(productRepository.findByRestaurantId(restaurantId, pageable)).thenReturn(emptyPage);

            Page<ProductResponseDto> resultPage = productServiceImpl
                    .findProductsByRestaurantId(restaurantIdString, pageable);

            assertNotNull(resultPage);
            assertTrue(resultPage.isEmpty());
            assertEquals(0, resultPage.getTotalElements());

            verify(restaurantService).findById(restaurantId);
            verify(productRepository).findByRestaurantId(restaurantId, pageable);
            verify(productMapper, never()).toResponseDto(any(Product.class));
        }

        @Test
        @DisplayName("Should return paged DTOs when restaurant has products")
        void should_ReturnDtoPage_When_RestaurantHasProducts() {
            Product product = new Product();
            product.setId(UUID.randomUUID());
            product.setName("Test Product");

            List<Product> productList = List.of(product);
            Page<Product> mockRepoPage = new PageImpl<>(productList, pageable, 1);

            ProductResponseDto expectedDto = new ProductResponseDto(
                    product.getId(), "Test Product", null, null, null, null, null, null
            );

            when(productRepository.findByRestaurantId(restaurantId, pageable)).thenReturn(mockRepoPage);
            when(restaurantService.findById(restaurantId)).thenReturn(mockRestaurant);
            when(productMapper.toResponseDto(product)).thenReturn(expectedDto);

            Page<ProductResponseDto> resultPage = productServiceImpl
                    .findProductsByRestaurantId(restaurantIdString, pageable);

            assertNotNull(resultPage);
            assertEquals(1, resultPage.getTotalElements());
            assertEquals(1, resultPage.getContent().size());
            assertEquals("Test Product", resultPage.getContent().get(0).name());

            verify(restaurantService).findById(restaurantId);
            verify(productRepository).findByRestaurantId(restaurantId, pageable);
            verify(productMapper).toResponseDto(product);
        }
    }

    @Nested
    @DisplayName("searchProducts() tests")
    class SearchProductsTests {

        private Pageable pageable;

        @BeforeEach
        void setUp() {
            pageable = PageRequest.of(0, 10);
        }

        @Test
        @DisplayName("Should build correct Example with available=true and map results")
        void should_BuildExampleWithAvailableTrue_When_ParametersProvided() {
            String name = "Pizza";
            String category = "ITALIANA";

            Product foundProduct = new Product();
            foundProduct.setId(UUID.randomUUID());
            foundProduct.setName("Pizza");
            List<Product> productList = List.of(foundProduct);
            Page<Product> mockRepoPage = new PageImpl<>(productList, pageable, 1);

            ProductResponseDto mappedDto = new ProductResponseDto(
                    foundProduct.getId(), "Pizza", null, null, true, null, null, null
            );

            when(productRepository.findAll(any(Example.class), eq(pageable))).thenReturn(mockRepoPage);
            when(productMapper.toResponseDto(foundProduct)).thenReturn(mappedDto);

            ArgumentCaptor<Example<Product>> exampleCaptor = ArgumentCaptor.forClass(Example.class);

            Page<ProductResponseDto> resultPage = productServiceImpl
                    .searchProducts(name, category, pageable);

            assertNotNull(resultPage);
            assertEquals(1, resultPage.getTotalElements());
            assertEquals("Pizza", resultPage.getContent().get(0).name());

            verify(productRepository).findAll(exampleCaptor.capture(), eq(pageable));
            Example<Product> capturedExample = exampleCaptor.getValue();

            assertEquals("Pizza", capturedExample.getProbe().getName());
            assertEquals("ITALIANA", capturedExample.getProbe().getCategory());

            assertTrue(
                    capturedExample.getProbe().getAvailable(),
                    "Search should always set 'available = true' in the probe"
            );

            verify(productMapper).toResponseDto(foundProduct);
        }

        @Test
        @DisplayName("Should build correct Example with available=true when parameters are null")
        void should_BuildExampleWithAvailableTrue_When_ParametersAreNull() {
            String category = "PIZZA";
            Page<Product> mockRepoPage = Page.empty(pageable);

            when(productRepository.findAll(any(Example.class), eq(pageable))).thenReturn(mockRepoPage);

            ArgumentCaptor<Example<Product>> exampleCaptor = ArgumentCaptor.forClass(Example.class);

            Page<ProductResponseDto> resultPage = productServiceImpl
                    .searchProducts(null, category, pageable);

            assertTrue(resultPage.isEmpty());

            verify(productRepository).findAll(exampleCaptor.capture(), eq(pageable));
            Example<Product> capturedExample = exampleCaptor.getValue();

            assertNull(capturedExample.getProbe().getName(), "Name should be null in the probe");
            assertEquals("PIZZA", capturedExample.getProbe().getCategory());

            assertTrue(
                    capturedExample.getProbe().getAvailable(),
                    "Search should always set 'available = true' in the probe"
            );

            verify(productMapper, never()).toResponseDto(any(Product.class));
        }
    }

    @Nested
    @DisplayName("isOwnerOfProductRestaurant() tests")
    class IsOwnerOfProductRestaurantTests {

        private UUID productId;
        private String productIdString;
        private UUID ownerRestaurantId;
        private Product existingProduct;

        @BeforeEach
        void setUp() {
            productId = UUID.randomUUID();
            productIdString = productId.toString();
            ownerRestaurantId = UUID.randomUUID();

            Restaurant ownerRestaurant = new Restaurant();
            ownerRestaurant.setId(ownerRestaurantId);

            existingProduct = new Product();
            existingProduct.setId(productId);
            existingProduct.setRestaurant(ownerRestaurant);
        }

        @Test
        @DisplayName("Should return true when security context restaurantId matches product's restaurantId")
        void should_ReturnTrue_When_IdsMatch() {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(ownerRestaurantId));
            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

            boolean isOwner = productServiceImpl.isOwnerOfProductRestaurant(productIdString);

            assertTrue(isOwner, "Should return true when IDs match");

            verify(securityService).getCurrentUserRestaurantId();
            verify(productRepository).findById(productId);
        }

        @Test
        @DisplayName("Should return false when security context restaurantId does NOT match")
        void should_ReturnFalse_When_IdsDoNotMatch() {
            UUID otherRestaurantId = UUID.randomUUID();

            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(otherRestaurantId));
            when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

            boolean isOwner = productServiceImpl.isOwnerOfProductRestaurant(productIdString);

            assertFalse(isOwner, "Should return false when IDs do not match");

            verify(securityService).getCurrentUserRestaurantId();
            verify(productRepository).findById(productId);
        }

        @Test
        @DisplayName("Should return false when security context has no restaurantId")
        void should_ReturnFalse_When_SecurityContextIsEmpty() {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.empty());

            boolean isOwner = productServiceImpl.isOwnerOfProductRestaurant(productIdString);

            assertFalse(isOwner, "Should return false when context is empty");

            verify(securityService).getCurrentUserRestaurantId();
            verify(productRepository, never()).findById(any(UUID.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product does not exist")
        void should_ThrowResourceNotFound_When_ProductNotFound() {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(ownerRestaurantId));
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                productServiceImpl.isOwnerOfProductRestaurant(productIdString);
            });

            verify(securityService).getCurrentUserRestaurantId();
            verify(productRepository).findById(productId);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when provided ID string is not a valid UUID")
        void should_ThrowIllegalArgumentException_When_IdStringIsInvalid() {
            String invalidUuidString = "not-a-uuid";

            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(ownerRestaurantId));

            assertThrows(IllegalArgumentException.class, () -> {
                productServiceImpl.isOwnerOfProductRestaurant(invalidUuidString);
            });

            verify(securityService).getCurrentUserRestaurantId();
            verify(productRepository, never()).findById(any(UUID.class));
        }
    }
}