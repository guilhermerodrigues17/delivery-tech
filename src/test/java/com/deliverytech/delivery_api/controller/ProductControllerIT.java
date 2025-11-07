package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.BaseIntegrationTest;
import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.model.Product;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.model.enums.ErrorCode;
import com.deliverytech.delivery_api.repository.ProductRepository;
import com.deliverytech.delivery_api.repository.RestaurantRepository;
import com.deliverytech.delivery_api.repository.UserRepository;
import com.deliverytech.delivery_api.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerIT extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private SecurityService securityService;

    private Restaurant restaurantA;
    private Restaurant restaurantB;
    private Product productA;

    @BeforeEach
    void setUp() {
        restaurantA = new Restaurant();
        restaurantA.setName("Restaurant A (Owner)");
        restaurantA.setCategory("BRASILEIRA");
        restaurantA.setAddress("Addr 1");
        restaurantA.setPhoneNumber("11111");
        restaurantA.setDeliveryTax(BigDecimal.ONE);
        restaurantA.setActive(true);
        restaurantA = restaurantRepository.saveAndFlush(restaurantA);

        restaurantB = new Restaurant();
        restaurantB.setName("Restaurant B (Other)");
        restaurantB.setCategory("ITALIANA");
        restaurantB.setAddress("Addr 2");
        restaurantB.setPhoneNumber("22222");
        restaurantB.setDeliveryTax(BigDecimal.ONE);
        restaurantB.setActive(true);
        restaurantB = restaurantRepository.saveAndFlush(restaurantB);

        productA = new Product();
        productA.setName("Pizza Margherita");
        productA.setDescription("Molho, queijo, manjericão");
        productA.setPrice(new BigDecimal("50.00"));
        productA.setCategory("PIZZA");
        productA.setAvailable(true);
        productA.setRestaurant(restaurantA);
        productA = productRepository.saveAndFlush(productA);
    }

    @Nested
    @DisplayName("POST /products tests")
    class CreateProductTests {

        private ProductRequestDto validDto;

        @BeforeEach
        void setUp() {
            validDto = new ProductRequestDto();
            validDto.setRestaurantId(restaurantA.getId());
            validDto.setName("New Valid Product");
            validDto.setDescription("This is a valid description");
            validDto.setPrice(new BigDecimal("25.00"));
            validDto.setCategory("Massas");
            validDto.setAvailable(true);
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when authenticated as CUSTOMER")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_RoleIsCustomer() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when RESTAURANT is not the owner")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnForbidden_When_RestaurantIsNotOwner() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantB.getId()));

            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when dto is invalid")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_DtoIsInvalid() throws Exception {
            validDto.setName("abc");
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details",
                            containsString("O nome do produto deve ter entre 5 e 50 caracteres")));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when provides non-existent restaurantId")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnResourceNotFound_When_RestaurantIdDoesNotExist() throws Exception {
            validDto.setRestaurantId(UUID.randomUUID());
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 201 - Created when RESTAURANT owner creates product")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnCreated_When_RestaurantIsOwner() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantA.getId()));

            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isCreated())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Produto criado com sucesso")))

                    .andExpect(jsonPath("$.data.id", notNullValue()))
                    .andExpect(jsonPath("$.data.name", is(validDto.getName())))
                    .andExpect(jsonPath("$.data.restaurantId", is(restaurantA.getId().toString())))

                    .andExpect(header().exists("Location"));
        }

        @Test
        @DisplayName("Should return 201 - Created when ADMIN creates product")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnCreated_When_AdminCreatesProduct() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isCreated())

                    .andExpect(jsonPath("$.success", is(true)))

                    .andExpect(jsonPath("$.data.id", notNullValue()))
                    .andExpect(jsonPath("$.data.name", is(validDto.getName())))
                    .andExpect(jsonPath("$.data.restaurantId", is(restaurantA.getId().toString())));
        }
    }

    @Nested
    @DisplayName("GET /products/search tests")
    class SearchProductsTests {

        private Product productB;

        @BeforeEach
        void setUp() {
            productB = new Product();
            productB.setName("Lasanha Bolonhesa");
            productB.setDescription("Massa fresca com molho");
            productB.setPrice(new BigDecimal("60.00"));
            productB.setCategory("MASSAS");
            productB.setAvailable(true);
            productB.setRestaurant(restaurantA);

            Product productC = new Product();
            productC.setName("Pizza Quatro Queijos (Inativa)");
            productC.setDescription("Produto indisponível");
            productC.setPrice(new BigDecimal("55.00"));
            productC.setCategory("PIZZA");
            productC.setAvailable(false);
            productC.setRestaurant(restaurantA);

            productRepository.saveAllAndFlush(List.of(productB, productC));
        }

        @Test
        @DisplayName("Should return 200 - OK with all ACTIVE products when no filters are provided")
        void should_ReturnOk_WithAllActiveProducts_When_NoFilters() throws Exception {

            mockMvc.perform(
                            get("/products/search")
                                    .param("page", "0")
                                    .param("size", "5")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements", is(2)))

                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[?(@.name == 'Pizza Margherita')]", hasSize(1)))
                    .andExpect(jsonPath("$.content[?(@.name == 'Lasanha Bolonhesa')]", hasSize(1)));
        }

        @Test
        @DisplayName("Should return 200 - OK with active products matching category")
        void should_ReturnOk_WithActiveProducts_When_FilteredByCategory() throws Exception {

            mockMvc.perform(
                            get("/products/search")
                                    .param("category", "PIZZA")
                                    .param("page", "0")
                                    .param("size", "5")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements", is(1)))

                    .andExpect(jsonPath("$.content[0].name", is(productA.getName())))
                    .andExpect(jsonPath("$.content[0].category", is(productA.getCategory())));
        }

        @Test
        @DisplayName("Should return 200 - OK with active products matching name")
        void should_ReturnOk_WithActiveProducts_When_FilteredByName() throws Exception {

            mockMvc.perform(
                            get("/products/search")
                                    .param("name", "Pizza")
                                    .param("page", "0")
                                    .param("size", "5")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements", is(1)))

                    .andExpect(jsonPath("$.content[0].name", is(productA.getName())));
        }

        @Test
        @DisplayName("Should return 200 - OK with active products matching name AND category")
        void should_ReturnOk_When_FilteredByNameAndCategory() throws Exception {

            mockMvc.perform(
                            get("/products/search")
                                    .param("name", "Lasanha")
                                    .param("category", "MASSAS")
                                    .param("page", "0")
                                    .param("size", "5")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements", is(1)))

                    .andExpect(jsonPath("$.content[0].name", is(productB.getName())))
                    .andExpect(jsonPath("$.content[0].category", is(productB.getCategory())));
        }

        @Test
        @DisplayName("Should return 200 - OK with empty page when no active products match filter")
        void should_ReturnOk_WithEmptyPage_When_NoActiveProductsMatch() throws Exception {

            mockMvc.perform(
                            get("/products/search")
                                    .param("name", "Sushi")
                                    .param("page", "0")
                                    .param("size", "5")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements", is(0)))

                    .andExpect(jsonPath("$.content", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /products/{id} tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return 400 - Bad Request when ID is not a valid UUID")
        void should_ReturnBadRequest_When_IdIsInvalidUUID() throws Exception {

            mockMvc.perform(get("/products/{id}", "not-a-valid-uuid"))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when ID does not exist")
        void should_ReturnResourceNotFound_When_IdDoesNotExist() throws Exception {

            mockMvc.perform(get("/products/{id}", UUID.randomUUID().toString()))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 200 - OK and Product data when ID exists")
        void should_ReturnOk_When_IdExists() throws Exception {

            mockMvc.perform(get("/products/{id}", productA.getId().toString()))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Operação realizada com sucesso")))

                    .andExpect(jsonPath("$.data.id", is(productA.getId().toString())))
                    .andExpect(jsonPath("$.data.name", is(productA.getName())))
                    .andExpect(jsonPath("$.data.restaurantName", is(productA.getRestaurant().getName())));
        }
    }

    @Nested
    @DisplayName("PUT /products/{id} tests")
    class UpdateProductTests {

        private ProductRequestDto updateDto;

        @BeforeEach
        void setUp() {
            updateDto = new ProductRequestDto();
            updateDto.setRestaurantId(restaurantA.getId());
            updateDto.setName("Updated Product Name");
            updateDto.setDescription("Updated Description");
            updateDto.setPrice(new BigDecimal("99.00"));
            updateDto.setCategory("MASSAS");
            updateDto.setAvailable(true);
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/products/{id}", productA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when authenticated as CUSTOMER")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_RoleIsCustomer() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/products/{id}", productA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when RESTAURANT is not the owner")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnForbidden_When_RestaurantIsNotOwner() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantB.getId()));
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/products/{id}", productA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when dto is invalid")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_DtoIsInvalid() throws Exception {
            updateDto.setPrice(BigDecimal.ZERO);
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/products/{id}", productA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", containsString("Preço deve ser maior que zero")));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when ADMIN updates non-existent product")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnResourceNotFound_When_IdDoesNotExist() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/products/{id}", UUID.randomUUID())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 422 - Unprocessable Entity when DTO restaurantId does not match")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnUnprocessableEntity_When_DtoRestaurantIdDoesNotMatch() throws Exception {
            updateDto.setRestaurantId(restaurantB.getId());
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/products/{id}", productA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isUnprocessableEntity())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNPROCESSABLE_ENTITY.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNPROCESSABLE_ENTITY.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", containsString("não pertence ao restaurante informado")));
        }

        @Test
        @DisplayName("Should return 200 - OK when RESTAURANT owner updates their own product")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnOk_When_RestaurantIsOwner() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantA.getId()));
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/products/{id}", productA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))

                    .andExpect(jsonPath("$.data.id", is(productA.getId().toString())))
                    .andExpect(jsonPath("$.data.name", is(updateDto.getName())))
                    .andExpect(jsonPath("$.data.price", is(99.00)));

            Product updated = productRepository.findById(productA.getId()).get();
            assertEquals(updateDto.getName(), updated.getName());
        }

        @Test
        @DisplayName("Should return 200 - OK when ADMIN updates any product")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_When_AdminUpdates() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/products/{id}", productA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))

                    .andExpect(jsonPath("$.data.name", is(updateDto.getName())));

            Product updated = productRepository.findById(productA.getId()).get();
            assertEquals(updateDto.getName(), updated.getName());
        }
    }

    @Nested
    @DisplayName("PATCH /products/{id}/status tests")
    class ToggleAvailabilityTests {

        private Product productB;

        @BeforeEach
        void setUp() {
            productB = new Product();
            productB.setName("Product B (Inactive)");
            productB.setDescription("Test");
            productB.setPrice(BigDecimal.TEN);
            productB.setCategory("PIZZA");
            productB.setAvailable(false);
            productB.setRestaurant(restaurantA);
            productB = productRepository.saveAndFlush(productB);
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {

            mockMvc.perform(patch("/products/{id}/status", productA.getId()))
                    .andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when authenticated as CUSTOMER")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_RoleIsCustomer() throws Exception {

            mockMvc.perform(patch("/products/{id}/status", productA.getId()))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when RESTAURANT is not the owner")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnForbidden_When_RestaurantIsNotOwner() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantB.getId()));

            mockMvc.perform(patch("/products/{id}/status", productA.getId()))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when updates non-existent product")
        @WithMockUser(roles = "ADMIN")
        void should_Return404_When_IdDoesNotExist() throws Exception {

            mockMvc.perform(patch("/products/{id}/status", UUID.randomUUID()))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 204 - No Content and toggle True to False when RESTAURANT is owner")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnNoContent_When_RestaurantIsOwner_TogglesTrueToFalse() throws Exception {
            assertTrue(productA.getAvailable(), "Pre-condition: Product should be active");

            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantA.getId()));

            mockMvc.perform(patch("/products/{id}/status", productA.getId()))
                    .andExpect(status().isNoContent());

            Product toggledProduct = productRepository.findById(productA.getId()).get();
            assertFalse(toggledProduct.getAvailable(), "Product availability should be toggled to false");
        }

        @Test
        @DisplayName("Should return 204 - No Content and toggle False to True when RESTAURANT is owner")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnNoContent_When_RestaurantIsOwner_TogglesFalseToTrue() throws Exception {
            assertFalse(productB.getAvailable(), "Pre-condition: Product should be inactive");

            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantA.getId()));

            mockMvc.perform(patch("/products/{id}/status", productB.getId()))
                    .andExpect(status().isNoContent());

            Product toggledProduct = productRepository.findById(productB.getId()).get();
            assertTrue(toggledProduct.getAvailable(), "Product availability should be toggled to true");
        }

        @Test
        @DisplayName("Should return 204 - No Content when ADMIN updates status")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnNoContent_When_AdminUpdatesStatus() throws Exception {
            assertTrue(productA.getAvailable());

            mockMvc.perform(patch("/products/{id}/status", productA.getId()))
                    .andExpect(status().isNoContent());

            Product toggledProduct = productRepository.findById(productA.getId()).get();
            assertFalse(toggledProduct.getAvailable(), "Product availability should be toggled to false");
        }
    }

    @Nested
    @DisplayName("DELETE /products/{id} tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {

            mockMvc.perform(delete("/products/{id}", productA.getId()))
                    .andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when authenticated as CUSTOMER")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_RoleIsCustomer() throws Exception {

            mockMvc.perform(delete("/products/{id}", productA.getId()))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when RESTAURANT is not the owner")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnForbidden_When_RestaurantIsNotOwner() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantB.getId()));

            mockMvc.perform(delete("/products/{id}", productA.getId()))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when ADMIN deletes non-existent product")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnResourceNotFound_When_IdDoesNotExist() throws Exception {

            mockMvc.perform(delete("/products/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 204 - No Content when RESTAURANT owner deletes their own product")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnNoContent_When_RestaurantIsOwner() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantA.getId()));

            mockMvc.perform(delete("/products/{id}", productA.getId()))
                    .andExpect(status().isNoContent());

            Optional<Product> deletedProduct = productRepository.findById(productA.getId());
            assertFalse(deletedProduct.isPresent(), "Product should be physically deleted from the database");
        }

        @Test
        @DisplayName("Should return 204 - No Content when ADMIN deletes any product")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnNoContent_When_AdminDeletes() throws Exception {

            mockMvc.perform(delete("/products/{id}", productA.getId()))
                    .andExpect(status().isNoContent());

            Optional<Product> deletedProduct = productRepository.findById(productA.getId());
            assertFalse(deletedProduct.isPresent(), "Product should be physically deleted from the database");
        }
    }
}