package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.BaseIntegrationTest;
import com.deliverytech.delivery_api.dto.request.ConsumerRequestDto;
import com.deliverytech.delivery_api.model.Consumer;
import com.deliverytech.delivery_api.model.Order;
import com.deliverytech.delivery_api.model.Restaurant;
import com.deliverytech.delivery_api.model.User;
import com.deliverytech.delivery_api.model.enums.ErrorCode;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import com.deliverytech.delivery_api.model.enums.Role;
import com.deliverytech.delivery_api.repository.ConsumerRepository;
import com.deliverytech.delivery_api.repository.OrderRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConsumerControllerIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private SecurityService securityService;

    private User userA;
    private Consumer consumerA;
    private User userB;
    private Consumer consumerB;
    private Restaurant restaurantA;
    private Order orderForConsumerA;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        consumerRepository.deleteAllInBatch();
        restaurantRepository.deleteAllInBatch();

        consumerA = new Consumer();
        consumerA.setName("Consumer A");
        consumerA.setEmail("userA@email.com");
        consumerA.setAddress("Rua A");
        consumerA.setPhoneNumber("111111111");
        consumerA.setActive(true);
        consumerA = consumerRepository.saveAndFlush(consumerA);

        userA = new User();
        userA.setName("User A");
        userA.setEmail("userA@email.com");
        userA.setPassword(passwordEncoder.encode("123"));
        userA.setRole(Role.CUSTOMER);
        userA.setActive(true);
        userRepository.saveAndFlush(userA);

        consumerB = new Consumer();
        consumerB.setName("Consumer B");
        consumerB.setEmail("userB@email.com");
        consumerB.setAddress("Rua B");
        consumerB.setPhoneNumber("222222222");
        consumerB.setActive(true);
        consumerB = consumerRepository.saveAndFlush(consumerB);

        userB = new User();
        userB.setName("User B");
        userB.setEmail("userB@email.com");
        userB.setPassword(passwordEncoder.encode("123"));
        userB.setRole(Role.CUSTOMER);
        userB.setActive(true);
        userRepository.saveAndFlush(userB);

        restaurantA = new Restaurant();
        restaurantA.setName("Test Restaurant");
        restaurantA.setCategory("BRASILEIRA");
        restaurantA.setAddress("Address A");
        restaurantA.setPhoneNumber("333333333");
        restaurantA.setDeliveryTax(new BigDecimal("10.00"));
        restaurantA.setActive(true);
        restaurantA = restaurantRepository.saveAndFlush(restaurantA);

        orderForConsumerA = new Order();
        orderForConsumerA.setConsumer(consumerA);
        orderForConsumerA.setRestaurant(restaurantA);
        orderForConsumerA.setDeliveryAddress(consumerA.getAddress());
        orderForConsumerA.setDeliveryTax(restaurantA.getDeliveryTax());
        orderForConsumerA.setStatus(OrderStatus.DELIVERED);
        orderForConsumerA.setSubtotal(new BigDecimal("50.00"));
        orderForConsumerA.setTotal(new BigDecimal("60.00"));
        orderForConsumerA = orderRepository.saveAndFlush(orderForConsumerA);
    }

    @Nested
    @DisplayName("GET /consumers/{id} tests")
    class GetConsumerByIdTests {

        @Test
        @DisplayName("Should return 403 - Forbidden when getting another consumer data")
        @WithMockUser(username = "userB@email.com", roles = "CUSTOMER")
        void should_ReturnForbidden_When_GettingAnotherConsumerData() throws Exception {
            mockMvc.perform(get("/consumers/{id}", consumerA.getId()))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when consumer ID not exists")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnResourceNotFound_When_ConsumerIdNotExists() throws Exception {
            mockMvc.perform(get("/consumers/{id}", UUID.randomUUID().toString()))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 200 - OK when getting own data")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnOK_When_GettingOwnData() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userA));

            mockMvc.perform(get("/consumers/{id}", consumerA.getId()))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(consumerA.getId().toString())))
                    .andExpect(jsonPath("$.data.email", is("userA@email.com")));
        }

        @Test
        @DisplayName("Should return 200 - OK when admin gets any consumer data")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOK_When_AdminGetsAnyConsumerData() throws Exception {
            mockMvc.perform(get("/consumers/{id}", consumerA.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(consumerA.getId().toString())));

            mockMvc.perform(get("/consumers/{id}", consumerB.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(consumerB.getId().toString())));
        }
    }

    @Nested
    @DisplayName("GET /consumers tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all active consumers when user is admin")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnAllActiveConsumers_When_UserIsAdmin() throws Exception {
            mockMvc.perform(get("/consumers"))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.content", notNullValue()));
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when user is not authenticated")
        void should_ReturnUnauthorized_When_UserIsNotAuthenticated() throws Exception {
            mockMvc.perform(get("/consumers"))
                    .andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when user is not admin")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_UserIsNotAdmin() throws Exception {
            mockMvc.perform(get("/consumers"))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
    }

    @Nested
    @DisplayName("GET /consumers/{consumerId}/orders tests")
    class FindOrdersByConsumerIdTests {

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {

            mockMvc.perform(get("/consumers/{id}/orders", consumerA.getId()))
                    .andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when CUSTOMER gets another consumer's orders")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_GettingAnotherConsumersOrders() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userB));

            mockMvc.perform(get("/consumers/{id}/orders", consumerA.getId()))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when ADMIN requests orders for non-existent consumer")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnResourceNotFound_When_AdminRequestsNonExistentConsumer() throws Exception {

            mockMvc.perform(get("/consumers/{id}/orders", UUID.randomUUID().toString()))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 200 - OK (and Paged Response) when CUSTOMER gets their own orders")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnOk_When_CustomerGetsOwnOrders() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userA));

            mockMvc.perform(get("/consumers/{id}/orders", consumerA.getId())
                            .param("page", "0")
                            .param("size", "10")
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.content", notNullValue()))
                    .andExpect(jsonPath("$.page.totalElements", is(1)))
                    .andExpect(jsonPath("$.page.totalPages", is(1)))

                    .andExpect(jsonPath("$.content[0].id", is(orderForConsumerA.getId().toString())))
                    .andExpect(jsonPath("$.content[0].restaurantName", is("Test Restaurant")))
                    .andExpect(jsonPath("$.content[0].status", is(OrderStatus.DELIVERED.name())));
        }

        @Test
        @DisplayName("Should return 200 - OK (and Paged Response) when ADMIN gets any consumer's orders")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_When_AdminGetsAnyConsumersOrders() throws Exception {
            mockMvc.perform(get("/consumers/{id}/orders", consumerA.getId()))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.content[0].id", is(orderForConsumerA.getId().toString())))
                    .andExpect(jsonPath("$.content[0].restaurantName", is("Test Restaurant")))
                    .andExpect(jsonPath("$.content[0].status", is(OrderStatus.DELIVERED.name())));
        }
    }

    @Nested
    @DisplayName("POST /consumers tests")
    class CreateConsumerTests {

        private ConsumerRequestDto validDto;

        @BeforeEach
        void setUp() {
            validDto = new ConsumerRequestDto();
            validDto.setName("Consumer");
            validDto.setEmail("consumer@email.com");
            validDto.setAddress("Rua B, 200");
            validDto.setPhoneNumber("11987654321");
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/consumers")
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
        @DisplayName("Should return 403 Forbidden when authenticated as CUSTOMER")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_RoleIsCustomer() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/consumers")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when email is invalid")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_EmailIsInvalid() throws Exception {
            validDto.setEmail("invalid-email.com");
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/consumers")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()))
                    .andExpect(jsonPath("$.error.details", containsString("E-mail inválido")));
        }


        @Test
        @DisplayName("Should return 409 - Conflict when email is duplicated")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnConflict_When_EmailIsDuplicated() throws Exception {
            Consumer existingConsumer = new Consumer();
            existingConsumer.setName("Consumer B");
            existingConsumer.setEmail("consumer@email.com");
            existingConsumer.setAddress("Rua A, 100");
            existingConsumer.setPhoneNumber("11000000000");
            existingConsumer.setActive(true);

            consumerRepository.saveAndFlush(existingConsumer);

            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/consumers")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isConflict())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.CONFLICT_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.CONFLICT_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()))
                    .andExpect(jsonPath("$.error.details", is("E-mail já está em uso")));
        }

        @Test
        @DisplayName("Should return 201 - Created when data is valid")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnCreated_When_DataIsValid() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(validDto);

            mockMvc.perform(
                            post("/consumers")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isCreated())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Cliente criado com sucesso")))

                    .andExpect(jsonPath("$.data.id", notNullValue()))
                    .andExpect(jsonPath("$.data.email", is(validDto.getEmail())))
                    .andExpect(jsonPath("$.data.name", is(validDto.getName())))
                    .andExpect(jsonPath("$.data.phoneNumber", is("11987654321")))

                    .andExpect(header().exists("Location"))
                    .andExpect(header().string("Location", containsString("/consumers/")));
        }
    }

    @Nested
    @DisplayName("PUT /consumers/{id} tests")
    class UpdateConsumerTests {

        private ConsumerRequestDto updateDto;

        @BeforeEach
        void setUp() {
            updateDto = new ConsumerRequestDto();
            updateDto.setName("Updated");
            updateDto.setEmail("updated@email.com");
            updateDto.setAddress("Updated, 1000");
            updateDto.setPhoneNumber("11900000000");
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/consumers/{id}", consumerA.getId())
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
        @DisplayName("Should return 403 - Forbidden when consumer is not owner")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_ConsumerIsNotOwner() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userB));

            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/consumers/{id}", consumerA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }


        @Test
        @DisplayName("Should return 400 - Bad Request when ADMIN sends invalid data")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_AdminSendsInvalidData() throws Exception {
            updateDto.setEmail("email");
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/consumers/{id}", consumerA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.VALIDATION_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.VALIDATION_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()))
                    .andExpect(jsonPath("$.error.details", containsString("E-mail inválido")));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when ADMIN updates non-existent consumer")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnResourceNotFound_When_AdminUpdatesNonExistentConsumer() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/consumers/{id}", UUID.randomUUID().toString())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 409 - Conflict when ADMIN updates email to a duplicated one")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnConflict_When_AdminUpdatesToDuplicatedEmail() throws Exception {
            updateDto.setEmail(consumerB.getEmail());
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/consumers/{id}", consumerA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isConflict())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.CONFLICT_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.CONFLICT_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()))
                    .andExpect(jsonPath("$.error.details", is("E-mail já está em uso")));
        }

        @Test
        @DisplayName("Should return 200 OK when CUSTOMER updates their own data")
        @WithMockUser(roles = "CUSTOMER")
        void should_Return200_When_CustomerUpdatesOwnData() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userA));

            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/consumers/{id}", consumerA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Dados atualizados com sucesso")))

                    .andExpect(jsonPath("$.data.id", is(consumerA.getId().toString())))
                    .andExpect(jsonPath("$.data.name", is(updateDto.getName())))
                    .andExpect(jsonPath("$.data.email", is(updateDto.getEmail())));
        }

        @Test
        @DisplayName("Should return 200 OK when ADMIN updates any consumer data")
        @WithMockUser(roles = "ADMIN")
        void should_Return200_When_AdminUpdatesAnyData() throws Exception {
            String jsonBody = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(
                            put("/consumers/{id}", consumerA.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonBody)
                    )
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.success", is(true)))

                    .andExpect(jsonPath("$.data.id", is(consumerA.getId().toString())))
                    .andExpect(jsonPath("$.data.name", is(updateDto.getName())))
                    .andExpect(jsonPath("$.data.email", is(updateDto.getEmail())));
        }
    }

    @Nested
    @DisplayName("DELETE /consumers/{id} tests")
    class DeleteConsumerTests {

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {

            mockMvc.perform(delete("/consumers/{id}", consumerA.getId()))
                    .andExpect(status().isUnauthorized())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.UNAUTHORIZED_ERROR.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.UNAUTHORIZED_ERROR.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 403 - Forbidden when consumer tries to delete another consumer")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnForbidden_When_ConsumerDeletesAnotherConsumer() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userB));

            mockMvc.perform(delete("/consumers/{id}", consumerA.getId()))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 404 - Not Found when ADMIN deletes non-existent consumer")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnResourceNotFound_When_AdminDeletesNonExistentConsumer() throws Exception {
            mockMvc.perform(delete("/consumers/{id}", UUID.randomUUID().toString()))
                    .andExpect(status().isNotFound())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.RESOURCE_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.RESOURCE_NOT_FOUND.getDefaultMessage())))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should return 204 - No Content when consumer deletes their own data")
        @WithMockUser(roles = "CUSTOMER")
        void should_ReturnNoContent_When_ConsumerDeletesOwnData() throws Exception {
            when(securityService.getCurrentUser()).thenReturn(Optional.of(userA));

            mockMvc.perform(delete("/consumers/{id}", consumerA.getId()))
                    .andExpect(status().isNoContent());

            Optional<Consumer> deletedConsumer = consumerRepository.findById(consumerA.getId());
            assertTrue(deletedConsumer.isPresent(), "Consumer should still exist (soft delete)");
            assertFalse(deletedConsumer.get().getActive(), "Consumer 'active' flag should be false");
        }

        @Test
        @DisplayName("Should return 204 - No Content when ADMIN deletes any consumer")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnNoContent_When_AdminDeletesAnyConsumer() throws Exception {
            mockMvc.perform(delete("/consumers/{id}", consumerA.getId()))
                    .andExpect(status().isNoContent());

            Optional<Consumer> deletedConsumer = consumerRepository.findById(consumerA.getId());
            assertTrue(deletedConsumer.isPresent(), "Consumer should still exist (soft delete)");
            assertFalse(deletedConsumer.get().getActive(), "Consumer 'active' flag should be false");
        }
    }
}