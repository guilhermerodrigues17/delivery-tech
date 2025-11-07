package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.BaseIntegrationTest;
import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.request.RestaurantStatusUpdateDto;
import com.deliverytech.delivery_api.model.*;
import com.deliverytech.delivery_api.model.enums.ErrorCode;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import com.deliverytech.delivery_api.repository.*;
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
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RestaurantControllerIT extends BaseIntegrationTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private SecurityService securityService;

    private Restaurant restaurantA;
    private Restaurant restaurantB;

    @BeforeEach
    void setUp() {
        restaurantA = new Restaurant();
        restaurantA.setName("Italian Place");
        restaurantA.setCategory("ITALIANA");
        restaurantA.setAddress("Addr 1");
        restaurantA.setPhoneNumber("11111");
        restaurantA.setDeliveryTax(new BigDecimal("10.00"));
        restaurantA.setActive(true);
        restaurantA = restaurantRepository.saveAndFlush(restaurantA);

        restaurantB = new Restaurant();
        restaurantB.setName("Brazilian Grill");
        restaurantB.setCategory("BRASILEIRA");
        restaurantB.setAddress("Addr 2");
        restaurantB.setPhoneNumber("22222");
        restaurantB.setDeliveryTax(BigDecimal.ONE);
        restaurantB.setActive(true);
        restaurantB = restaurantRepository.saveAndFlush(restaurantB);
    }

    @Nested
    @DisplayName("POST /restaurants tests")
    class CreateRestaurantTests {

        private RestaurantRequestDto validDto;

        @BeforeEach
        void setUpDto() {
            validDto = new RestaurantRequestDto();
            validDto.setName("New Test Restaurant");
            validDto.setCategory("ITALIANA");
            validDto.setAddress("123 Test Street");
            validDto.setPhoneNumber("11988776655");
            validDto.setDeliveryTax(new BigDecimal("5.00"));
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/restaurants")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when authenticated as CUSTOMER")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_RoleIsCustomer() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/restaurants")
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
        @DisplayName("Should return 400 - Bad Request when name is blank")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_NameIsBlank() throws Exception {
            validDto.setName("");
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/restaurants")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", containsString("Nome não pode estar em branco")));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when category is invalid")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_CategoryIsInvalid() throws Exception {
            validDto.setCategory("INVALID_CATEGORY");
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/restaurants")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", containsString("Categoria inválida")));
        }

        @Test
        @DisplayName("Should return 409 - Conflict when name is duplicated")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnConflict_When_NameIsDuplicated() throws Exception {
            Restaurant existing = new Restaurant();
            existing.setName("New Test Restaurant");
            existing.setCategory("BRASILEIRA");
            existing.setAddress("Old Address");
            existing.setPhoneNumber("111111111");
            existing.setDeliveryTax(BigDecimal.ONE);
            existing.setActive(true);
            restaurantRepository.saveAndFlush(existing);

            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/restaurants")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isConflict())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.CONFLICT_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.CONFLICT_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", is("Nome de restaurante já está em uso")));
        }

        @Test
        @DisplayName("Should return 201 - Created when data is valid")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnCreated_When_DataIsValid() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(validDto);

            ResultActions result = mockMvc.perform(
                    post("/restaurants")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
            );
            result.andExpect(status().isCreated())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Restaurante cadastrado com sucesso")))

                    .andExpect(jsonPath("$.data.id", notNullValue()))
                    .andExpect(jsonPath("$.data.name", is(validDto.getName())))
                    .andExpect(jsonPath("$.data.category", is(validDto.getCategory())))
                    .andExpect(jsonPath("$.data.phoneNumber", is("11988776655")))
                    .andExpect(jsonPath("$.data.active", is(true)))

                    .andExpect(header().exists("Location"))
                    .andExpect(header().string("Location", containsString("/restaurants/")));
        }
    }

    @Nested
    @DisplayName("GET /restaurants/{id} tests")
    class GetRestaurantByIdTests {

        @Test
        @DisplayName("Should return 400 - Bad Request when ID is not a valid UUID")
        void should_ReturnBadRequest_When_IdIsInvalidUUID() throws Exception {
            String invalidId = "not-a-uuid";

            mockMvc.perform(get("/restaurants/{id}", invalidId))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when ID does not exist")
        void should_ReturnResourceNotFound_When_IdDoesNotExist() throws Exception {

            mockMvc.perform(get("/restaurants/{id}", UUID.randomUUID().toString()))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 200 - OK and Restaurant data when ID exists")
        void should_ReturnOk_When_IdExists() throws Exception {

            mockMvc.perform(
                            get("/restaurants/{id}", restaurantA.getId())
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Operação realizada com sucesso")))

                    .andExpect(jsonPath("$.data.id", is(restaurantA.getId().toString())))
                    .andExpect(jsonPath("$.data.name", is(restaurantA.getName())))
                    .andExpect(jsonPath("$.data.category", is(restaurantA.getCategory())))
                    .andExpect(jsonPath("$.data.active", is(restaurantA.getActive())));
        }
    }

    @Nested
    @DisplayName("GET /restaurants (search) tests")
    class SearchRestaurantTests {

        private Restaurant restaurantInactive;

        @BeforeEach
        void setUp() {
            restaurantInactive = new Restaurant();
            restaurantInactive.setName("Old Italian");
            restaurantInactive.setCategory("ITALIANA");
            restaurantInactive.setAddress("Addr 3");
            restaurantInactive.setPhoneNumber("33333");
            restaurantInactive.setDeliveryTax(BigDecimal.ONE);
            restaurantInactive.setActive(false);
            restaurantInactive = restaurantRepository.saveAndFlush(restaurantInactive);
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when category is invalid")
        void should_ReturnBadRequest_When_CategoryIsInvalid() throws Exception {

            mockMvc.perform(get("/restaurants")
                            .param("category", "INVALID_CATEGORY")
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", containsString("Categoria inválida")));
        }

        @Test
        @DisplayName("Should return 200 - OK with only active restaurants when 'active' param is omitted")
        void should_ReturnOk_WithActiveRestaurants_When_ActiveParamIsOmitted() throws Exception {

            mockMvc.perform(get("/restaurants")
                            .param("page", "0")
                            .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.content", notNullValue()))

                    .andExpect(jsonPath("$.page.totalElements", is(2)))
                    .andExpect(jsonPath("$.content[?(@.name == 'Italian Place')]", hasSize(1)))
                    .andExpect(jsonPath("$.content[?(@.name == 'Brazilian Grill')]", hasSize(1)));
        }

        @Test
        @DisplayName("Should return 200 - OK with filtered results when all params are provided")
        void should_ReturnOk_WithFilteredResults_When_AllParamsProvided() throws Exception {

            mockMvc.perform(get("/restaurants")
                            .param("name", "Italian")
                            .param("category", "ITALIANA")
                            .param("active", "true")
                            .param("page", "0")
                            .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.page.totalElements", is(1)))
                    .andExpect(jsonPath("$.content[0].name", is(restaurantA.getName())));
        }

        @Test
        @DisplayName("Should return 200 - OK with only inactive restaurants when active=false")
        void should_ReturnOk_WithInactiveRestaurants_When_ActiveIsFalse() throws Exception {

            mockMvc.perform(get("/restaurants")
                            .param("active", "false")
                            .param("page", "0")
                            .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.content", notNullValue()))

                    .andExpect(jsonPath("$.page.totalElements", is(1)))
                    .andExpect(jsonPath("$.content[0].name", is(restaurantInactive.getName())));
        }
    }

    @Nested
    @DisplayName("GET /{restaurantId}/products tests")
    class GetProductsByRestaurantIdTests {

        private Product productA;

        @BeforeEach
        void setUp() {
            productA = new Product();
            productA.setName("Pizza");
            productA.setDescription("Test Pizza");
            productA.setPrice(BigDecimal.TEN);
            productA.setCategory("PIZZA");
            productA.setAvailable(true);
            productA.setRestaurant(restaurantA);
            productA = productRepository.saveAndFlush(productA);
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when restaurantId is not a valid UUID")
        void should_ReturnBadRequest_When_IdIsInvalidUUID() throws Exception {
            mockMvc.perform(get("/restaurants/{id}/products", "not-a-uuid"))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when restaurantId does not exist")
        void should_ReturnResourceNotFound_When_IdDoesNotExist() throws Exception {
            mockMvc.perform(get("/restaurants/{id}/products", UUID.randomUUID().toString()))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 200 - OK with empty page when restaurant has no products")
        void should_ReturnOk_WithEmptyPage_When_RestaurantHasNoProducts() throws Exception {
            mockMvc.perform(get("/restaurants/{id}/products", restaurantB.getId())
                            .param("page", "0")
                            .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.page.totalElements", is(0)));
        }

        @Test
        @DisplayName("Should return 200 - OK with paged products when restaurant has products")
        void should_ReturnOk_WithPagedProducts_When_RestaurantHasProducts() throws Exception {
            mockMvc.perform(get("/restaurants/{id}/products", restaurantA.getId())
                            .param("page", "0")
                            .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.page.totalElements", is(1)))

                    .andExpect(jsonPath("$.content[0].id", is(productA.getId().toString())))
                    .andExpect(jsonPath("$.content[0].name", is(productA.getName())))
                    .andExpect(jsonPath("$.content[0].restaurantId", is(restaurantA.getId().toString())));
        }
    }

    @Nested
    @DisplayName("GET /{restaurantId}/orders tests")
    class GetOrdersByRestaurantIdTests {

        private Consumer customerA;
        private Order orderA;

        @BeforeEach
        void setUp() {
            customerA = new Consumer();
            customerA.setName("Customer");
            customerA.setEmail("customer@email.com");
            customerA.setAddress("Addr 3");
            customerA.setPhoneNumber("33333");
            customerA.setActive(true);
            customerA = consumerRepository.saveAndFlush(customerA);

            orderA = new Order();
            orderA.setConsumer(customerA);
            orderA.setRestaurant(restaurantA);
            orderA.setDeliveryAddress(customerA.getAddress());
            orderA.setDeliveryTax(restaurantA.getDeliveryTax());
            orderA.setStatus(OrderStatus.DELIVERED);
            orderA.setSubtotal(BigDecimal.TEN);
            orderA.setTotal(BigDecimal.TEN);
            orderA = orderRepository.saveAndFlush(orderA);
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {

            mockMvc.perform(get("/restaurants/{id}/orders", restaurantA.getId()))
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

            mockMvc.perform(get("/restaurants/{id}/orders", restaurantA.getId()))
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

            mockMvc.perform(get("/restaurants/{id}/orders", restaurantA.getId()))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }


        @Test
        @DisplayName("Should return 404 - Not Found when requests non-existent restaurant")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnResourceNotFound_When_RestaurantNotFound() throws Exception {

            mockMvc.perform(get("/restaurants/{id}/orders", UUID.randomUUID().toString()))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 200 - OK (and Paged Response) when RESTAURANT is owner")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnOk_When_RestaurantIsOwner() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantA.getId()));

            mockMvc.perform(get("/restaurants/{id}/orders", restaurantA.getId())
                            .param("page", "0")
                            .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.page.totalElements", is(1)))

                    .andExpect(jsonPath("$.content[0].id", is(orderA.getId().toString())))
                    .andExpect(jsonPath("$.content[0].status", is(OrderStatus.DELIVERED.name())));
        }

        @Test
        @DisplayName("Should return 200 OK (and Paged Response) when ADMIN requests any restaurant")
        @WithMockUser(roles = "ADMIN")
        void should_Return200_When_AdminRequests() throws Exception {

            mockMvc.perform(get("/restaurants/{id}/orders", restaurantA.getId())
                            .param("page", "0")
                            .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.page.totalElements", is(1)))

                    .andExpect(jsonPath("$.content[0].id", is(orderA.getId().toString())))
                    .andExpect(jsonPath("$.content[0].status", is(OrderStatus.DELIVERED.name())));
        }
    }

    @Nested
    @DisplayName("GET /{id}/delivery-tax tests")
    class CalculateDeliveryTaxTests {

        @Test
        @DisplayName("Should return 400 - Bad Request when ID is not a valid UUID")
        void should_ReturnBadRequest_When_IdIsInvalidUUID() throws Exception {

            mockMvc.perform(get("/restaurants/{id}/delivery-tax", "not-a-uuid")
                            .param("cep", "06401-000")
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when 'cep' parameter is missing")
        void should_ReturnBadRequest_When_CepParameterIsMissing() throws Exception {

            mockMvc.perform(get("/restaurants/{id}/delivery-tax", restaurantA.getId()))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when 'cep' format is invalid")
        void should_ReturnBadRequest_When_CepFormatIsInvalid() throws Exception {

            mockMvc.perform(get("/restaurants/{id}/delivery-tax", restaurantA.getId())
                            .param("cep", "12345")
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when restaurantId does not exist")
        void should_ReturnResourceNotFound_When_IdDoesNotExist() throws Exception {
            mockMvc.perform(get("/restaurants/{id}/delivery-tax", UUID.randomUUID().toString())
                            .param("cep", "06401-000")
                    )
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 422 - Unprocessable Entity when CEP zone is invalid")
        void should_ReturnUnprocessableEntity_When_CepZoneIsInvalid() throws Exception {
            String validCepFormatInvalidZone = "99999-999";

            mockMvc.perform(get("/restaurants/{id}/delivery-tax", restaurantA.getId())
                            .param("cep", validCepFormatInvalidZone)
                    )
                    .andExpect(status().isUnprocessableEntity())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNPROCESSABLE_ENTITY.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNPROCESSABLE_ENTITY.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details",
                            is("Desculpe, este restaurante não realiza entregas para o CEP informado.")));
        }

        @Test
        @DisplayName("Should return 200 - OK with base tax for SHORT_DISTANCE cep")
        void should_ReturnOk_WithBaseTax_ForShortDistance() throws Exception {

            mockMvc.perform(get("/restaurants/{id}/delivery-tax", restaurantA.getId())
                            .param("cep", "06401-000")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.deliveryTax", is(10.00)));
        }

        @Test
        @DisplayName("Should return 200 - OK with base tax + 5.00 for MEDIUM_DISTANCE cep")
        void should_ReturnOk_WithMediumTax_ForMediumDistance() throws Exception {

            mockMvc.perform(get("/restaurants/{id}/delivery-tax", restaurantA.getId())
                            .param("cep", "06451-000")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.deliveryTax", is(15.00)));
        }

        @Test
        @DisplayName("Should return 200 - OK with base tax + 10.00 for LONG_DISTANCE cep")
        void should_ReturnOk_WithLongTax_ForLongDistance() throws Exception {

            mockMvc.perform(get("/restaurants/{id}/delivery-tax", restaurantA.getId())
                            .param("cep", "06471-000")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.deliveryTax", is(20.00)));
        }
    }

    @Nested
    @DisplayName("GET /restaurants/nearby tests")
    class FindRestaurantsNearbyTests {

        @Test
        @DisplayName("Should return 400 - Bad Request when 'cep' parameter is missing")
        void should_ReturnBadRequest_When_CepParameterIsMissing() throws Exception {

            mockMvc.perform(get("/restaurants/search/nearby"))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when 'cep' format is invalid")
        void should_ReturnBadRequest_When_CepFormatIsInvalid() throws Exception {

            mockMvc.perform(get("/restaurants/search/nearby")
                            .param("cep", "12345-ABC")
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", containsString("Formato de CEP inválido")));
        }

        @Test
        @DisplayName("Should return 200 - OK with paged results when 'cep' is valid")
        void should_ReturnOk_When_CepIsValid() throws Exception {

            mockMvc.perform(get("/restaurants/search/nearby")
                            .param("cep", "06401-000")
                            .param("page", "0")
                            .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.content", notNullValue()))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.page.totalElements", is(2)))

                    .andExpect(jsonPath("$.content[0].id", is(restaurantA.getId().toString())));
        }
    }

    @Nested
    @DisplayName("PUT /restaurants/{id} tests")
    class UpdateRestaurantTests {

        private RestaurantRequestDto updateDto;

        @BeforeEach
        void setUp() {
            updateDto = new RestaurantRequestDto();
            updateDto.setName("Updated Name");
            updateDto.setCategory("ITALIANA");
            updateDto.setAddress("Updated Address");
            updateDto.setPhoneNumber("11912341234");
            updateDto.setDeliveryTax(new BigDecimal("15.00"));
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put("/restaurants/{id}", restaurantA.getId())
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

            mockMvc.perform(put("/restaurants/{id}", restaurantA.getId())
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

            mockMvc.perform(put("/restaurants/{id}", restaurantA.getId())
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
            updateDto.setName("");
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put("/restaurants/{id}", restaurantA.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", containsString("Nome não pode estar em branco")));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when updates non-existent restaurant")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnResourceNotFound_When_IdDoesNotExist() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put("/restaurants/{id}", UUID.randomUUID())
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
        @DisplayName("Should return 409 - Conflict when updates name to a duplicated one")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnConflict_When_NameIsDuplicated() throws Exception {
            updateDto.setName(restaurantB.getName());
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put("/restaurants/{id}", restaurantA.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                    )
                    .andExpect(status().isConflict())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.CONFLICT_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.CONFLICT_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", is("Nome de restaurante já está em uso")));
        }

        @Test
        @DisplayName("Should return 200 - OK when RESTAURANT owner updates their own data")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnOk_When_RestaurantIsOwner() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantA.getId()));
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put("/restaurants/{id}", restaurantA.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Dados atualizados com sucesso")))

                    .andExpect(jsonPath("$.data.id", is(restaurantA.getId().toString())))
                    .andExpect(jsonPath("$.data.name", is(updateDto.getName())))
                    .andExpect(jsonPath("$.data.phoneNumber", is("11912341234")));

            Restaurant updated = restaurantRepository.findById(restaurantA.getId()).get();
            assertEquals(updateDto.getName(), updated.getName());
        }

        @Test
        @DisplayName("Should return 200 - OK when ADMIN updates any restaurant")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_When_AdminUpdatesAnyRestaurant() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put("/restaurants/{id}", restaurantA.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.name", is(updateDto.getName())));

            Restaurant updated = restaurantRepository.findById(restaurantA.getId()).get();
            assertEquals(updateDto.getName(), updated.getName());
        }
    }

    @Nested
    @DisplayName("PATCH /restaurants/{id}/status tests")
    class UpdateStatusActiveTests {
        private RestaurantStatusUpdateDto updateStatusDto;

        @BeforeEach
        void setUp() {
            updateStatusDto = new RestaurantStatusUpdateDto();
            updateStatusDto.setActive(false);
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(updateStatusDto);

            mockMvc.perform(
                            patch("/restaurants/{id}/status", restaurantA.getId())
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
            String jsonBody = objectMapper.writeValueAsString(updateStatusDto);

            mockMvc.perform(
                            patch("/restaurants/{id}/status", restaurantA.getId())
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
            String jsonBody = objectMapper.writeValueAsString(updateStatusDto);

            mockMvc.perform(
                            patch("/restaurants/{id}/status", restaurantA.getId())
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
        @DisplayName("Should return 400 - Bad Request when 'active' field is null")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_DtoFieldIsNull() throws Exception {
            updateStatusDto.setActive(null);
            String jsonBody = objectMapper.writeValueAsString(updateStatusDto);

            mockMvc.perform(
                            patch("/restaurants/{id}/status", restaurantA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", containsString("O campo 'active' é obrigatório")));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when 'active' field is not a boolean")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_ActiveIsNotBoolean() throws Exception {
            String jsonBody = "{\"active\": \"true\"}";

            mockMvc.perform(
                            patch("/restaurants/{id}/status", restaurantA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 204 - No Content when RESTAURANT owner updates status")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnNoContent_When_RestaurantIsOwner() throws Exception {
            assertTrue(restaurantA.getActive());

            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantA.getId()));
            String jsonBody = objectMapper.writeValueAsString(updateStatusDto);

            mockMvc.perform(
                            patch("/restaurants/{id}/status", restaurantA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isNoContent());

            Restaurant updatedRestaurant = restaurantRepository.findById(restaurantA.getId()).get();
            assertFalse(updatedRestaurant.getActive(), "Restaurant 'active' flag should be updated to false");
        }

        @Test
        @DisplayName("Should return 204 - No Content when ADMIN updates status")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnNoContent_When_AdminUpdatesStatus() throws Exception {
            assertTrue(restaurantA.getActive());

            String jsonBody = objectMapper.writeValueAsString(updateStatusDto);

            mockMvc.perform(
                            patch("/restaurants/{id}/status", restaurantA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isNoContent());

            Restaurant updatedRestaurant = restaurantRepository.findById(restaurantA.getId()).get();
            assertFalse(updatedRestaurant.getActive(), "Restaurant 'active' flag should be updated to false");
        }
    }
}