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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
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

    private User userCustomerA;
    private Consumer customerA;

    private User userCustomerB;
    private Consumer customerB;

    private Restaurant restaurantA;
    private Restaurant restaurantB;

    private Order orderA;

    @BeforeEach
    void setUp() {
        userCustomerA = new User();
        userCustomerA.setName("Test CustomerA");
        userCustomerA.setEmail("customerA@email.com");
        userCustomerA.setPassword(passwordEncoder.encode("123"));
        userCustomerA.setRole(Role.CUSTOMER);
        userCustomerA.setActive(true);
        userRepository.saveAndFlush(userCustomerA);

        customerA = new Consumer();
        customerA.setName("Test ConsumerA");
        customerA.setEmail("customerA@email.com");
        customerA.setAddress("Street A");
        customerA.setPhoneNumber("111111111");
        customerA.setActive(true);
        customerA = consumerRepository.saveAndFlush(customerA);

        userCustomerB = new User();
        userCustomerB.setName("Test CustomerB");
        userCustomerB.setEmail("customerB@email.com");
        userCustomerB.setPassword(passwordEncoder.encode("123"));
        userCustomerB.setRole(Role.CUSTOMER);
        userCustomerB.setActive(true);
        userRepository.saveAndFlush(userCustomerB);

        customerB = new Consumer();
        customerB.setName("Test ConsumerB");
        customerB.setEmail("customerB@email.com");
        customerB.setAddress("Street B");
        customerB.setPhoneNumber("111111111");
        customerB.setActive(true);
        customerB = consumerRepository.saveAndFlush(customerB);

        restaurantA = new Restaurant();
        restaurantA.setName("Restaurant A");
        restaurantA.setCategory("BRASILEIRA");
        restaurantA.setAddress("Address A");
        restaurantA.setPhoneNumber("333333333");
        restaurantA.setDeliveryTax(new BigDecimal("10.00"));
        restaurantA.setActive(true);
        restaurantA = restaurantRepository.saveAndFlush(restaurantA);

        restaurantB = new Restaurant();
        restaurantB.setName("Restaurant B");
        restaurantB.setCategory("ITALIANA");
        restaurantB.setAddress("Address B");
        restaurantB.setPhoneNumber("444444444");
        restaurantB.setDeliveryTax(new BigDecimal("12.00"));
        restaurantB.setActive(true);
        restaurantB = restaurantRepository.saveAndFlush(restaurantB);

        orderA = new Order();
        orderA.setConsumer(customerA);
        orderA.setRestaurant(restaurantA);
        orderA.setDeliveryAddress(customerA.getAddress());
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
        private Product productA;
        private Product productB;

        @BeforeEach
        void setUp() {
            productA = new Product();
            productA.setName("Product A");
            productA.setRestaurant(restaurantA);
            productA.setPrice(BigDecimal.TEN);
            productA.setCategory("TEST");
            productA.setDescription("Test");
            productA.setAvailable(true);
            productA = productRepository.saveAndFlush(productA);

            productB = new Product();
            productB.setName("Product B");
            productB.setRestaurant(restaurantB);
            productB.setPrice(BigDecimal.TEN);
            productB.setCategory("TEST");
            productB.setDescription("Test");
            productB.setAvailable(true);
            productB = productRepository.saveAndFlush(productB);

            orderRequest = new OrderRequestDto(
                    customerA.getId(),
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
                    .andExpect(jsonPath("$.data.restaurantName", is(restaurantA.getName())))
                    .andExpect(jsonPath("$.data.consumerName", is(customerA.getName())))
                    .andExpect(jsonPath("$.data.status", is(OrderStatus.PENDING.name())))
                    .andExpect(jsonPath("$.data.items", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /orders/{id} tests")
    class FindOrderByIdTests {

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
        @DisplayName("Should return 403 - Forbidden when CUSTOMER tries to get another user's order")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_CustomerGetsAnotherUsersOrder() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userCustomerB));

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
        @DisplayName("Should return 200 - OK when CUSTOMER gets their own order")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnOk_When_CustomerGetsOwnOrder() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userCustomerA));

            mockMvc.perform(get("/orders/{id}", orderA.getId()))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(orderA.getId().toString())))
                    .andExpect(jsonPath("$.data.consumerName", is(customerA.getName())));
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

    @Nested
    @DisplayName("GET /orders (search) tests")
    class SearchOrdersTests {

        private Order orderB;
        private Order orderC;
        private LocalDate today;

        @BeforeEach
        void setUp() {
            today = LocalDate.now();

            orderB = new Order();
            orderB.setDeliveryAddress(customerA.getAddress());
            orderB.setSubtotal(BigDecimal.TEN);
            orderB.setDeliveryTax(BigDecimal.TEN);
            orderB.setTotal(BigDecimal.TEN);
            orderB.setStatus(OrderStatus.PENDING);
            orderB.setConsumer(customerA);
            orderB.setRestaurant(restaurantA);

            orderC = new Order();
            orderC.setDeliveryAddress(customerA.getAddress());
            orderC.setSubtotal(BigDecimal.TEN);
            orderC.setDeliveryTax(BigDecimal.TEN);
            orderC.setTotal(BigDecimal.TEN);
            orderC.setStatus(OrderStatus.DELIVERED);
            orderC.setConsumer(customerA);
            orderC.setRestaurant(restaurantA);

            orderRepository.saveAllAndFlush(List.of(orderB, orderC));
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {

            mockMvc.perform(get("/orders"))
                    .andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as CUSTOMER")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_RoleIsCustomer() throws Exception {

            mockMvc.perform(get("/orders"))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when 'status' is invalid")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_StatusIsInvalid() throws Exception {

            mockMvc.perform(
                            get("/orders")
                                    .param("status", "INVALID_STATUS")
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when 'startDate' format is invalid")
        @WithMockUser(username = "admin@email.com", roles = "ADMIN")
        void should_ReturnBadRequest_When_DateFormatIsInvalid() throws Exception {

            mockMvc.perform(
                            get("/orders")
                                    .param("startDate", "01-01-2025")
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 200 - OK with all orders when no filters provided")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_WithAllOrders_When_NoFilters() throws Exception {

            mockMvc.perform(
                            get("/orders")
                                    .param("page", "0")
                                    .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.page.totalElements", is(3)))
                    .andExpect(jsonPath("$.content", hasSize(3)));
        }

        @Test
        @DisplayName("Should return 200 - OK filtered by status")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_When_FilteredByStatus() throws Exception {

            mockMvc.perform(
                            get("/orders")
                                    .param("status", "DELIVERED")
                                    .param("page", "0")
                                    .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.page.totalElements", is(1)))
                    .andExpect(jsonPath("$.content[?(@.status == 'DELIVERED')]", hasSize(1)));
        }

        @Test
        @DisplayName("Should return 200 - OK filtered by date range")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_When_FilteredByDate() throws Exception {

            mockMvc.perform(
                            get("/orders")
                                    .param("startDate", today.format(DateTimeFormatter.ISO_DATE))
                                    .param("endDate", today.format(DateTimeFormatter.ISO_DATE))
                                    .param("page", "0")
                                    .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.page.totalElements", is(3)))
                    .andExpect(jsonPath("$.content", hasSize(3)));
        }

        @Test
        @DisplayName("Should return 200 - OK filtered by date AND status")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_When_FilteredByDateAndStatus() throws Exception {

            mockMvc.perform(
                            get("/orders")
                                    .param("startDate", today.format(DateTimeFormatter.ISO_DATE))
                                    .param("endDate", today.format(DateTimeFormatter.ISO_DATE))
                                    .param("status", "DELIVERED")
                                    .param("page", "0")
                                    .param("size", "5")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.page.totalElements", is(1)))
                    .andExpect(jsonPath("$.content[0].id", is(orderC.getId().toString())));
        }
    }

    @Nested
    @DisplayName("POST /orders/calculate tests")
    class CalculateOrderTotalTests {

        private Product productA;
        private Product productB;
        private OrderRequestDto dto;

        @BeforeEach
        void setUp() {
            productA = new Product();
            productA.setName("Product A");
            productA.setRestaurant(restaurantA);
            productA.setPrice(new BigDecimal("10.00"));
            productA.setCategory("TEST");
            productA.setDescription("Test");
            productA.setAvailable(true);
            productA = productRepository.saveAndFlush(productA);

            productB = new Product();
            productB.setName("Product B");
            productB.setRestaurant(restaurantB);
            productB.setPrice(new BigDecimal("10.00"));
            productB.setCategory("TEST");
            productB.setDescription("Test");
            productB.setAvailable(true);
            productB = productRepository.saveAndFlush(productB);

            dto = new OrderRequestDto(customerA.getId(), restaurantA.getId(), List.of(
                    new OrderItemRequestDto(productA.getId(), 1)
            ));
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(dto);

            mockMvc.perform(
                            post("/orders/calculate")
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
        @DisplayName("Should return 403 - Forbidden when authenticated as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnForbidden_When_RoleIsAdmin() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(dto);

            mockMvc.perform(
                            post("/orders/calculate")
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
        @DisplayName("Should return 400 - Bad Request when DTO is invalid")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnBadRequest_When_DtoIsInvalid() throws Exception {
            dto.setRestaurantId(null);
            String jsonBody = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post("/orders/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonBody))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", containsString("O ID do restaurante é obrigatório")));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when restaurantId does not exist")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnResourceNotFound_When_RestaurantIdDoesNotExist() throws Exception {
            dto.setRestaurantId(UUID.randomUUID());
            String jsonBody = objectMapper.writeValueAsString(dto);

            mockMvc.perform(
                            post("/orders/calculate")
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
        @DisplayName("Should return 404 - Not Found when a productId does not exist")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnResourceNotFound_When_ProductIdDoesNotExist() throws Exception {
            dto.setItems(List.of(new OrderItemRequestDto(UUID.randomUUID(), 1)));
            String jsonBody = objectMapper.writeValueAsString(dto);

            mockMvc.perform(
                            post("/orders/calculate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 422 - Unprocessable Entity when product does not belong to restaurant")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnUnprocessableEntity_When_ProductDoesNotBelongToRestaurant() throws Exception {
            dto.setItems(List.of(new OrderItemRequestDto(productB.getId(), 1)));
            String jsonBody = objectMapper.writeValueAsString(dto);

            mockMvc.perform(
                            post("/orders/calculate")
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
        @DisplayName("Should return 200 - OK with correct totals when data is valid")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnOk_WithCorrectTotals() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(dto);

            mockMvc.perform(
                            post("/orders/calculate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))

                    .andExpect(jsonPath("$.data.subtotal", is(10.00)))
                    .andExpect(jsonPath("$.data.deliveryTax", is(10.00)))
                    .andExpect(jsonPath("$.data.total", is(20.00)));
        }
    }

    @Nested
    @DisplayName("DELETE /orders/{id} tests")
    class CancelOrderTests {

        private Order orderDelivered;

        @BeforeEach
        void setUp() {
            orderDelivered = new Order();
            orderDelivered.setDeliveryAddress(customerA.getAddress());
            orderDelivered.setSubtotal(BigDecimal.TEN);
            orderDelivered.setDeliveryTax(BigDecimal.TEN);
            orderDelivered.setTotal(BigDecimal.TEN);
            orderDelivered.setStatus(OrderStatus.DELIVERED);
            orderDelivered.setConsumer(customerA);
            orderDelivered.setRestaurant(restaurantA);

            orderRepository.saveAndFlush(orderDelivered);
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {

            mockMvc.perform(delete("/orders/{id}", orderA.getId()))
                    .andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when authenticated as RESTAURANT")
        @WithMockUser(roles = "RESTAURANT")
        void should_ReturnForbidden_When_RoleIsRestaurant() throws Exception {

            mockMvc.perform(delete("/orders/{id}", orderA.getId()))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when CUSTOMER is not the owner")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_CustomerIsNotOwner() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userCustomerB));

            mockMvc.perform(delete("/orders/{id}", orderA.getId()))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when ID is not a valid UUID")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_IdIsInvalidUUID() throws Exception {

            mockMvc.perform(delete("/orders/{id}", "not-a-uuid"))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when ADMIN deletes non-existent order")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnResourceNotFound_When_IdDoesNotExist() throws Exception {

            mockMvc.perform(delete("/orders/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 422 - Unprocessable Entity when order status is not cancellable")
        @WithMockUser(roles = "ADMIN")
        void should_Return422_When_OrderStatusIsNotCancellable() throws Exception {
            assertSame(OrderStatus.DELIVERED, orderDelivered.getStatus());

            mockMvc.perform(delete("/orders/{id}", orderDelivered.getId()))
                    .andExpect(status().isUnprocessableEntity())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNPROCESSABLE_ENTITY.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNPROCESSABLE_ENTITY.getDefaultMessage())))
                    .andExpect(jsonPath("$.error.details", is("Não é possível cancelar o pedido com status 'DELIVERED'.")));
        }

        @Test
        @DisplayName("Should return 204 - No Content when CUSTOMER owner cancels order")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnNoContent_When_CustomerIsOwner() throws Exception {
            assertSame(OrderStatus.PENDING, orderA.getStatus());
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userCustomerA));

            mockMvc.perform(delete("/orders/{id}", orderA.getId()))
                    .andExpect(status().isNoContent());

            Order cancelledOrder = orderRepository.findById(orderA.getId()).get();
            assertEquals(OrderStatus.CANCELED, cancelledOrder.getStatus());
        }

        @Test
        @DisplayName("Should return 204 - No Content when ADMIN cancels order")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnNoContent_When_AdminCancelsOrder() throws Exception {
            assertSame(OrderStatus.PENDING, orderA.getStatus());

            mockMvc.perform(delete("/orders/{id}", orderA.getId()))
                    .andExpect(status().isNoContent());

            Order cancelledOrder = orderRepository.findById(orderA.getId()).get();
            assertEquals(OrderStatus.CANCELED, cancelledOrder.getStatus());
        }
    }
}