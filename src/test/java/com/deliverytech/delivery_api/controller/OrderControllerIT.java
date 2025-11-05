package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.BaseIntegrationTest;
import com.deliverytech.delivery_api.dto.request.OrderItemRequestDto;
import com.deliverytech.delivery_api.dto.request.OrderRequestDto;
import com.deliverytech.delivery_api.dto.request.OrderStatusUpdateRequestDto;
import com.deliverytech.delivery_api.model.*;
import com.deliverytech.delivery_api.model.enums.ErrorCode;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import com.deliverytech.delivery_api.model.enums.Role;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class OrderControllerIT extends BaseIntegrationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConsumerRepository consumerRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private SecurityService securityService;

    private User userCustomer;
    private Consumer consumerCustomer;
    private Restaurant restaurantA;
    private Product productA;
    private Restaurant restaurantB;
    private Product productB;
    private Order orderA;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        consumerRepository.deleteAllInBatch();
        restaurantRepository.deleteAllInBatch();

        userCustomer = new User();
        userCustomer.setName("Test Customer");
        userCustomer.setEmail("customer@email.com");
        userCustomer.setPassword(passwordEncoder.encode("123"));
        userCustomer.setRole(Role.CUSTOMER);
        userCustomer.setActive(true);
        userRepository.saveAndFlush(userCustomer);

        consumerCustomer = new Consumer();
        consumerCustomer.setName("Test Consumer");
        consumerCustomer.setEmail("customer@email.com");
        consumerCustomer.setAddress("Street A");
        consumerCustomer.setPhoneNumber("111111111");
        consumerCustomer.setActive(true);
        consumerCustomer = consumerRepository.saveAndFlush(consumerCustomer);

        restaurantA = new Restaurant();
        restaurantA.setName("Restaurant A");
        restaurantA.setCategory("BRASILEIRA");
        restaurantA.setAddress("Address A");
        restaurantA.setPhoneNumber("333333333");
        restaurantA.setDeliveryTax(new BigDecimal("10.00"));
        restaurantA.setActive(true);
        restaurantA = restaurantRepository.saveAndFlush(restaurantA);

        productA = new Product();
        productA.setName("Product A");
        productA.setDescription("Description A");
        productA.setPrice(new BigDecimal("20.00"));
        productA.setCategory("Category A");
        productA.setAvailable(true);
        productA.setRestaurant(restaurantA);
        productA = productRepository.saveAndFlush(productA);

        restaurantB = new Restaurant();
        restaurantB.setName("Restaurant B");
        restaurantB.setCategory("ITALIANA");
        restaurantB.setAddress("Address B");
        restaurantB.setPhoneNumber("444444444");
        restaurantB.setDeliveryTax(new BigDecimal("12.00"));
        restaurantB.setActive(true);
        restaurantB = restaurantRepository.saveAndFlush(restaurantB);

        productB = new Product();
        productB.setName("Product B");
        productB.setDescription("Description B");
        productB.setPrice(new BigDecimal("50.00"));
        productB.setCategory("Category B");
        productB.setAvailable(true);
        productB.setRestaurant(restaurantB);
        productB = productRepository.saveAndFlush(productB);

        orderA = new Order();
        orderA.setConsumer(consumerCustomer);
        orderA.setRestaurant(restaurantA);
        orderA.setDeliveryAddress(consumerCustomer.getAddress());
        orderA.setDeliveryTax(restaurantA.getDeliveryTax());
        orderA.setStatus(OrderStatus.PENDING);
        orderA.setSubtotal(new BigDecimal("20.00"));
        orderA.setTotal(new BigDecimal("30.00"));
        orderA = orderRepository.saveAndFlush(orderA);
    }

    @Nested
    @DisplayName("POST /orders tests")
    class CreateOrderTests {

        private OrderRequestDto orderRequest;

        @BeforeEach
        void setUp() {
            orderRequest = new OrderRequestDto(
                    consumerCustomer.getId(),
                    restaurantA.getId(),
                    List.of(new OrderItemRequestDto(productA.getId(), 1))
            );
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(orderRequest);

            mockMvc.perform(
                            post("/orders")
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
        @DisplayName("Should return 403 - Forbidden when authenticated with invalid role")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnForbidden_When_RoleIsInvalid() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(orderRequest);

            mockMvc.perform(
                            post("/orders")
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
        @DisplayName("Should return 400 - Bad Request when data is invalid")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnBadRequest_When_DataIsInvalid() throws Exception {
            orderRequest.setConsumerId(null);
            String jsonBody = objectMapper.writeValueAsString(orderRequest);

            mockMvc.perform(
                            post("/orders")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", containsString("O ID do cliente é obrigatório")));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when Restaurant ID does not exist")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnResourceNotFound_When_RestaurantIdNotFound() throws Exception {
            orderRequest.setRestaurantId(UUID.randomUUID());
            String jsonBody = objectMapper.writeValueAsString(orderRequest);

            mockMvc.perform(
                            post("/orders")
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
        @DisplayName("Should return 422 - Unprocessable Entity when Product does not belong to Restaurant")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnUnprocessableEntity_When_ProductNotBelongToRestaurant() throws Exception {
            orderRequest.setItems(List.of(new OrderItemRequestDto(productB.getId(), 1)));
            String jsonBody = objectMapper.writeValueAsString(orderRequest);

            mockMvc.perform(
                            post("/orders")
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
        @DisplayName("Should return 201 - Created when authenticated as CUSTOMER and data is valid")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnCreated_When_DataIsValid() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(orderRequest);

            mockMvc.perform(
                    post("/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody)
            )
                    .andExpect(status().isCreated())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Pedido criado com sucesso")))

                    .andExpect(header().exists("Location"))
                    .andExpect(header().string("Location", containsString("/orders/")))

                    .andExpect(jsonPath("$.data.id", notNullValue()))
                    .andExpect(jsonPath("$.data.restaurantName", is("Restaurant A")))
                    .andExpect(jsonPath("$.data.consumerName", is("Test Consumer")))
                    .andExpect(jsonPath("$.data.status", is("PENDING")))

                    .andExpect(jsonPath("$.data.subtotal", is(20.00)))
                    .andExpect(jsonPath("$.data.deliveryTax", is(10.00)))
                    .andExpect(jsonPath("$.data.total", is(30.00)))

                    .andExpect(jsonPath("$.data.items", hasSize(1)))
                    .andExpect(jsonPath("$.data.items[0].productName", is("Product A")))
                    .andExpect(jsonPath("$.data.items[0].quantity", is(1)));
        }
    }

    @Nested
    @DisplayName("GET /orders/{id} tests")
    class FindOrderByIdTests {
        private User userAdmin;
        private User userConsumerB;
        private Consumer consumerB;

        @BeforeEach
        void setUp() {
            userAdmin = new User();
            userAdmin.setName("Admin User");
            userAdmin.setEmail("admin@email.com");
            userAdmin.setPassword(passwordEncoder.encode("123"));
            userAdmin.setRole(Role.ADMIN);
            userAdmin.setActive(true);
            userRepository.saveAndFlush(userAdmin);
            
            userConsumerB = new User();
            userConsumerB.setName("Consumer B");
            userConsumerB.setEmail("consumerB@email.com");
            userConsumerB.setPassword(passwordEncoder.encode("123"));
            userConsumerB.setRole(Role.CUSTOMER);
            userConsumerB.setActive(true);
            userRepository.saveAndFlush(userConsumerB);
            
            consumerB = new Consumer();
            consumerB.setName("Consumer B");
            consumerB.setEmail("userCustomerB");
            consumerB.setAddress("Street B");
            consumerB.setPhoneNumber("66778899");
            consumerB.setActive(true);

            User userRestaurantA = new User();
            userRestaurantA.setName("Restaurant A User");
            userRestaurantA.setEmail("restauranta@email.com");
            userRestaurantA.setPassword(passwordEncoder.encode("123"));
            userRestaurantA.setRole(Role.RESTAURANT);
            userRestaurantA.setRestaurant(restaurantA);
            userRestaurantA.setActive(true);
            userRepository.saveAndFlush(userRestaurantA);

            User userRestaurantB = new User();
            userRestaurantB.setName("Restaurant B User");
            userRestaurantB.setEmail("restaurantb@email.com");
            userRestaurantB.setPassword(passwordEncoder.encode("123"));
            userRestaurantB.setRole(Role.RESTAURANT);
            userRestaurantB.setRestaurant(restaurantB);
            userRestaurantB.setActive(true);
            userRepository.saveAndFlush(userRestaurantB);
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            
            mockMvc.perform(get("/orders/{id}", orderA.getId()))
                    .andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
        
        @Test
        @DisplayName("Should return 404 - Not Found when ADMIN requests non-existent order")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnResourceNotFound_When_AdminRequestsNonExistentOrder() throws Exception {
            
            mockMvc.perform(get("/orders/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }
        
        @Test
        @DisplayName("Should return 403 - Forbidden when CUSTOMER tries to get another user's order")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_CustomerGetsAnotherUsersOrder() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userConsumerB));
            
            mockMvc.perform(get("/orders/{id}", orderA.getId()))
                    .andExpect(status().isForbidden())
                    
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when RESTAURANT tries to get another restaurant's order")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnForbidden_When_RestaurantGetsAnotherRestaurantOrder() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantB.getId()));

            mockMvc.perform(get("/orders/{id}", orderA.getId()))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 200 - OK when CUSTOMER gets their own order")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnOk_When_CustomerGetsOwnOrder() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userCustomer));

            mockMvc.perform(get("/orders/{id}", orderA.getId()))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(orderA.getId().toString())))
                    .andExpect(jsonPath("$.data.consumerName", is(consumerCustomer.getName())));
        }

        @Test
        @DisplayName("Should return 200 - OK when RESTAURANT gets their own order")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnOk_When_RestaurantGetsOwnOrder() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantA.getId()));

            mockMvc.perform(get("/orders/{id}", orderA.getId()))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(orderA.getId().toString())))
                    .andExpect(jsonPath("$.data.restaurantName", is(restaurantA.getName())));
        }

        @Test
        @DisplayName("Should return 200 - OK when ADMIN gets any order")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_When_AdminGetsAnyOrder() throws Exception {
            mockMvc.perform(get("/orders/{id}", orderA.getId()))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(orderA.getId().toString())));
        }
    }

    @Nested
    @DisplayName("PATCH /orders/{id} tests")
    class UpdateOrderStatusTests {

        private OrderStatusUpdateRequestDto statusUpdateDto;

        @BeforeEach
        void setUp() {
            statusUpdateDto = new OrderStatusUpdateRequestDto();
            statusUpdateDto.setStatus(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(statusUpdateDto);

            mockMvc.perform(
                            patch("/orders/{id}", orderA.getId())
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
            String jsonBody = objectMapper.writeValueAsString(statusUpdateDto);

            mockMvc.perform(
                            patch("/orders/{id}", orderA.getId())
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
        @DisplayName("Should return 403 - Forbidden when RESTAURANT updates another's order")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnForbidden_When_RestaurantIsNotOwner() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantB.getId()));

            String jsonBody = objectMapper.writeValueAsString(statusUpdateDto);

            mockMvc.perform(
                            patch("/orders/{id}", orderA.getId())
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
        @DisplayName("Should return 422 - Unprocessable Entity when transition is invalid")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnUnprocessableEntity_When_TransitionIsInvalid() throws Exception {
            statusUpdateDto.setStatus(OrderStatus.DELIVERED);
            String jsonBody = objectMapper.writeValueAsString(statusUpdateDto);

            mockMvc.perform(
                            patch("/orders/{id}", orderA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isUnprocessableEntity())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNPROCESSABLE_ENTITY.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNPROCESSABLE_ENTITY.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", is("Não é possível mudar de PENDING para DELIVERED")));
        }

        @Test
        @DisplayName("Should return 200 - OK when RESTAURANT owner updates status")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnOk_When_RestaurantOwnerUpdatesStatus() throws Exception {
            when(securityService.getCurrentUserRestaurantId()).thenReturn(Optional.of(restaurantA.getId()));

            String jsonBody = objectMapper.writeValueAsString(statusUpdateDto);

            mockMvc.perform(
                            patch("/orders/{id}", orderA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("O status do pedido foi atualizado")))
                    .andExpect(jsonPath("$.data.status", is(OrderStatus.CONFIRMED.name())));

            Order updatedOrder = orderRepository.findById(orderA.getId()).get();
            assertEquals(OrderStatus.CONFIRMED, updatedOrder.getStatus());
        }

        @Test
        @DisplayName("Should return 200 - OK when ADMIN updates status")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_When_AdminUpdatesStatus() throws Exception {
            statusUpdateDto.setStatus(OrderStatus.PREPARING);
            String jsonBody = objectMapper.writeValueAsString(statusUpdateDto);

            mockMvc.perform(
                            patch("/orders/{id}", orderA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.status", is(OrderStatus.PREPARING.name())));

            Order updatedOrder = orderRepository.findById(orderA.getId()).get();
            assertEquals(OrderStatus.PREPARING, updatedOrder.getStatus());
        }
    }
}