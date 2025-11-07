package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.BaseIntegrationTest;
import com.deliverytech.delivery_api.model.*;
import com.deliverytech.delivery_api.model.enums.ErrorCode;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import com.deliverytech.delivery_api.model.enums.Role;
import com.deliverytech.delivery_api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportControllerIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Nested
    @DisplayName("GET /reports/sales-by-restaurant tests")
    class GetSalesByRestaurantTests {

        private User userCustomer;
        private Restaurant restaurantA;
        private Restaurant restaurantB;

        @BeforeEach
        void setUp() {

            userCustomer = new User();
            userCustomer.setName("Customer");
            userCustomer.setEmail("customer@email.com");
            userCustomer.setPassword(passwordEncoder.encode("123"));
            userCustomer.setRole(Role.CUSTOMER);
            userCustomer.setActive(true);
            userRepository.saveAndFlush(userCustomer);

            restaurantA = new Restaurant();
            restaurantA.setName("Restaurant A");
            restaurantA.setCategory("BRASILEIRA");
            restaurantA.setAddress("Addr 1");
            restaurantA.setPhoneNumber("11111");
            restaurantA.setDeliveryTax(BigDecimal.ONE);
            restaurantA.setActive(true);
            restaurantA = restaurantRepository.saveAndFlush(restaurantA);

            restaurantB = new Restaurant();
            restaurantB.setName("Restaurant B");
            restaurantB.setCategory("ITALIANA");
            restaurantB.setAddress("Addr 2");
            restaurantB.setPhoneNumber("22222");
            restaurantB.setDeliveryTax(BigDecimal.ONE);
            restaurantB.setActive(true);
            restaurantB = restaurantRepository.saveAndFlush(restaurantB);

            Consumer consumer = new Consumer();
            consumer.setName("Consumer");
            consumer.setEmail("consumer@email.com");
            consumer.setAddress("Addr 3");
            consumer.setPhoneNumber("33333");
            consumer.setActive(true);
            consumer = consumerRepository.saveAndFlush(consumer);

            Order order1 = new Order(null, null, null, "Addr 3", new BigDecimal("100.00"),
                    BigDecimal.ONE, new BigDecimal("101.00"), OrderStatus.DELIVERED, consumer, restaurantA, null);
            Order order2 = new Order(null, null, null, "Addr 3", new BigDecimal("50.00"),
                    BigDecimal.ONE, new BigDecimal("51.00"), OrderStatus.DELIVERED, consumer, restaurantA, null);
            Order order3 = new Order(null, null, null, "Addr 3", new BigDecimal("200.00"),
                    BigDecimal.ONE, new BigDecimal("201.00"), OrderStatus.DELIVERED, consumer, restaurantB, null);
            Order order4 = new Order(null, null, null, "Addr 3", new BigDecimal("30.00"),
                    BigDecimal.ONE, new BigDecimal("31.00"), OrderStatus.PENDING, consumer, restaurantA, null);

            orderRepository.saveAllAndFlush(List.of(order1, order2, order3, order4));
        }

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {

            mockMvc.perform(get("/reports/sales-by-restaurant"))
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

            mockMvc.perform(get("/reports/sales-by-restaurant"))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 200 - OK and aggregated sales list when authenticated as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_WithSalesList_When_AdminAuthenticated() throws Exception {

            mockMvc.perform(get("/reports/sales-by-restaurant"))

                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$", hasSize(2)))

                    .andExpect(jsonPath("$[0].restaurantName", is("Restaurant B")))
                    .andExpect(jsonPath("$[0].totalSales", is(201.00))) // 200.00 + 1.00 tax = 201.00
                    .andExpect(jsonPath("$[1].restaurantName", is("Restaurant A")))
                    .andExpect(jsonPath("$[1].totalSales", is(152.00))); // (100+1) + (50+1) = 152.00
        }
    }

    @Nested
    @DisplayName("GET /reports/top-selling-products security and empty tests")
    class GetTopSellingProductsSecurityTests {

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {

            mockMvc.perform(get("/reports/top-selling-products"))
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

            mockMvc.perform(get("/reports/top-selling-products"))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 200 - OK with empty list when no delivered orders exist")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_WithEmptyList_When_NoDeliveredOrders() throws Exception {
            mockMvc.perform(get("/reports/top-selling-products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /reports/top-selling-products with data tests")
    class GetTopSellingProductsDataTests {

        private Product productA;
        private Product productB;

        @BeforeEach
        void setUp() {
            Restaurant restaurant = new Restaurant();
            restaurant.setName("Restaurant");
            restaurant.setCategory("BRASILEIRA");
            restaurant.setAddress("Addr 1");
            restaurant.setPhoneNumber("11111");
            restaurant.setDeliveryTax(BigDecimal.ONE);
            restaurant.setActive(true);
            restaurant = restaurantRepository.saveAndFlush(restaurant);

            Consumer consumer = new Consumer();
            consumer.setName("Consumer");
            consumer.setEmail("consumer@email.com");
            consumer.setAddress("Addr 3");
            consumer.setPhoneNumber("33333");
            consumer.setActive(true);
            consumer = consumerRepository.saveAndFlush(consumer);

            productA = new Product(null, "Pizza", "Desc A", BigDecimal.TEN, "PIZZA",
                    true, restaurant);

            productB = new Product(null, "Soda", "Desc B", BigDecimal.ONE, "BEBIDAS",
                    true, restaurant);

            productRepository.saveAllAndFlush(List.of(productA, productB));

            Order order1 = new Order(null, null, null, "Addr", BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, OrderStatus.DELIVERED, consumer, restaurant, null);
            order1 = orderRepository.saveAndFlush(order1);

            OrderItem item1 = new OrderItem(null, 10, BigDecimal.TEN, BigDecimal.ZERO, order1, productA);
            OrderItem item2 = new OrderItem(null, 5, BigDecimal.ONE, BigDecimal.ZERO, order1, productB);
            orderItemRepository.saveAllAndFlush(List.of(item1, item2));

            Order order2 = new Order(null, null, null, "Addr", BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, OrderStatus.DELIVERED, consumer, restaurant, null);
            order2 = orderRepository.saveAndFlush(order2);

            OrderItem item3 = new OrderItem(null, 3, BigDecimal.TEN, BigDecimal.ZERO, order2, productA);
            orderItemRepository.saveAndFlush(item3);

            Order order3 = new Order(null, null, null, "Addr", BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, OrderStatus.PENDING, consumer, restaurant, null);
            order3 = orderRepository.saveAndFlush(order3);

            OrderItem item4 = new OrderItem(null, 100, BigDecimal.TEN, BigDecimal.ZERO, order3, productA);
            orderItemRepository.saveAndFlush(item4);
        }

        @Test
        @DisplayName("Should return 200 - OK and aggregated product list when authenticated as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_WithSalesList_When_AdminAuthenticated() throws Exception {

            mockMvc.perform(get("/reports/top-selling-products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))

                    .andExpect(jsonPath("$[0].productName", is(productA.getName())))
                    .andExpect(jsonPath("$[0].totalSold", is(13)))

                    .andExpect(jsonPath("$[1].productName", is(productB.getName())))
                    .andExpect(jsonPath("$[1].totalSold", is(5)));
        }
    }

    @Nested
    @DisplayName("GET /reports/active-consumers security and empty tests")
    class GetActiveConsumersSecurityTests {

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {

            mockMvc.perform(get("/reports/active-consumers"))
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

            mockMvc.perform(get("/reports/active-consumers"))
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 200 - OK with empty list when no delivered orders exist")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_WithEmptyList_When_NoDeliveredOrders() throws Exception {

            mockMvc.perform(get("/reports/active-consumers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /reports/active-consumers with data tests")
    class GetActiveConsumersDataTests {

        private Consumer consumerA;
        private Consumer consumerB;

        @BeforeEach
        void setUp() {
            User userAdmin = new User();
            userAdmin.setName("Admin");
            userAdmin.setEmail("admin@email.com");
            userAdmin.setPassword(passwordEncoder.encode("123"));
            userAdmin.setRole(Role.ADMIN);
            userAdmin.setActive(true);
            userRepository.saveAndFlush(userAdmin);

            Restaurant restaurant = new Restaurant();
            restaurant.setName("Restaurant");
            restaurant.setCategory("BRASILEIRA");
            restaurant.setAddress("Addr 1");
            restaurant.setPhoneNumber("11111");
            restaurant.setDeliveryTax(BigDecimal.ONE);
            restaurant.setActive(true);
            restaurant = restaurantRepository.saveAndFlush(restaurant);

            consumerA = new Consumer(null, "Mais Ativo", "ativo@email.com", "111",
                    "Addr A", true, null);

            consumerB = new Consumer(null, "Menos Ativo", "menosativo@email.com", "222",
                    "Addr B", true, null);

            Consumer consumerC = new Consumer(null, "Inativo", "inativo@email.com", "333",
                    "Addr C", true, null);

            consumerRepository.saveAllAndFlush(List.of(consumerA, consumerB, consumerC));

            Order orderA1 = new Order(null, null, null, "Addr", BigDecimal.TEN,
                    BigDecimal.ONE, BigDecimal.TEN, OrderStatus.DELIVERED, consumerA, restaurant, null);

            Order orderA2 = new Order(null, null, null, "Addr", BigDecimal.TEN,
                    BigDecimal.ONE, BigDecimal.TEN, OrderStatus.DELIVERED, consumerA, restaurant, null);

            Order orderA3 = new Order(null, null, null, "Addr", BigDecimal.TEN,
                    BigDecimal.ONE, BigDecimal.TEN, OrderStatus.DELIVERED, consumerA, restaurant, null);

            Order orderA4 = new Order(null, null, null, "Addr", BigDecimal.TEN,
                    BigDecimal.ONE, BigDecimal.TEN, OrderStatus.PENDING, consumerA, restaurant, null);

            Order orderB1 = new Order(null, null, null, "Addr", BigDecimal.TEN,
                    BigDecimal.ONE, BigDecimal.TEN, OrderStatus.DELIVERED, consumerB, restaurant, null);

            orderRepository.saveAllAndFlush(List.of(orderA1, orderA2, orderA3, orderA4, orderB1));
        }

        @Test
        @DisplayName("Should return 200 - OK and aggregated list of active consumers, sorted by total orders")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_WithAggregatedList_When_AdminAuthenticated() throws Exception {

            mockMvc.perform(get("/reports/active-consumers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))

                    .andExpect(jsonPath("$[0].consumerName", is(consumerA.getName())))
                    .andExpect(jsonPath("$[0].consumerEmail", is(consumerA.getEmail())))
                    .andExpect(jsonPath("$[0].totalOrders", is(3)))

                    .andExpect(jsonPath("$[1].consumerName", is(consumerB.getName())))
                    .andExpect(jsonPath("$[1].consumerEmail", is(consumerB.getEmail())))
                    .andExpect(jsonPath("$[1].totalOrders", is(1)));
        }
    }

    @Nested
    @DisplayName("GET /reports/orders-by-period security and validation tests")
    class GetOrdersByPeriodSecurityTests {

        @Test
        @DisplayName("Should return 401 - Unauthorized when not authenticated")
        void should_ReturnUnauthorize_When_NotAuthenticated() throws Exception {

            mockMvc.perform(
                            get("/reports/orders-by-period")
                                    .param("startDate", "2025-01-01")
                                    .param("endDate", "2025-01-31")
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

            mockMvc.perform(
                            get("/reports/orders-by-period")
                                    .param("startDate", "2025-01-01")
                                    .param("endDate", "2025-01-31")
                    )
                    .andExpect(status().isForbidden())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.FORBIDDEN_ACCESS.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.FORBIDDEN_ACCESS.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when 'startDate' parameter is missing")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_StartDateIsMissing() throws Exception {

            mockMvc.perform(
                            get("/reports/orders-by-period")
                                    .param("endDate", "2025-01-31")
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when 'endDate' parameter is missing")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_EndDateIsMissing() throws Exception {

            mockMvc.perform(
                            get("/reports/orders-by-period")
                                    .param("startDate", "2025-01-01")
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }

        @Test
        @DisplayName("Should return 400 - Bad Request when date format is invalid")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnBadRequest_When_DateFormatIsInvalid() throws Exception {

            mockMvc.perform(
                            get("/reports/orders-by-period")
                                    .param("startDate", "01-01-2025")
                                    .param("endDate", "31-01-2025")
                    )
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", notNullValue()))

                    .andExpect(jsonPath("$.error.code", is(ErrorCode.BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.error.message", is(ErrorCode.BAD_REQUEST.getDefaultMessage())));
        }
    }

    @Nested
    @DisplayName("GET /reports/orders-by-period with data tests")
    class GetOrdersByPeriodDataTests {

        private LocalDate today;

        @BeforeEach
        void setUp() {
            today = LocalDate.now();

            Restaurant restaurant = new Restaurant(null, "Rest", "CAT", "Addr", "123",
                    BigDecimal.ZERO, true, null, null, null);
            restaurant = restaurantRepository.saveAndFlush(restaurant);

            Consumer consumer = new Consumer(null, "Cons", "cons@email.com", "123",
                    "Addr", true, null);
            consumer = consumerRepository.saveAndFlush(consumer);

            Order order1 = new Order(null, null, null, "Addr", new BigDecimal("100"),
                    BigDecimal.ZERO, new BigDecimal("100"), OrderStatus.DELIVERED, consumer, restaurant, null);

            Order order2 = new Order(null, null, null, "Addr", new BigDecimal("50"),
                    BigDecimal.ZERO, new BigDecimal("50"), OrderStatus.PENDING, consumer, restaurant, null);

            orderRepository.saveAllAndFlush(List.of(order1, order2));
        }

        @Test
        @DisplayName("Should return 200 - OK with list filtered by date and status")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_WithFilteredList_ByDateAndStatus() throws Exception {

            mockMvc.perform(
                            get("/reports/orders-by-period")
                                    .param("startDate", today.format(DateTimeFormatter.ISO_DATE))
                                    .param("endDate", today.format(DateTimeFormatter.ISO_DATE))
                                    .param("status", "DELIVERED")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))

                    .andExpect(jsonPath("$[0].totalOrders", is(1)))
                    .andExpect(jsonPath("$[0].totalSales", is(100.00)))
                    .andExpect(jsonPath("$[0].status", is("DELIVERED")));
        }

        @Test
        @DisplayName("Should return 200 - OK with list filtered by date and all statuses")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_WithFilteredList_ByDateAndNullStatus() throws Exception {

            mockMvc.perform(
                            get("/reports/orders-by-period")
                                    .param("startDate", today.format(DateTimeFormatter.ISO_DATE))
                                    .param("endDate", today.format(DateTimeFormatter.ISO_DATE))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))

                    .andExpect(jsonPath("$[?(@.status == 'DELIVERED')].totalOrders", contains(1)))
                    .andExpect(jsonPath("$[?(@.status == 'DELIVERED')].totalSales", contains(100.00)))

                    .andExpect(jsonPath("$[?(@.status == 'PENDING')].totalOrders", contains(1)))
                    .andExpect(jsonPath("$[?(@.status == 'PENDING')].totalSales", contains(50.00)));
        }

        @Test
        @DisplayName("Should return 200 - OK with empty list when no orders match date range")
        @WithMockUser(roles = "ADMIN")
        void should_ReturnOk_WithEmptyList_When_NoOrdersMatchDate() throws Exception {

            LocalDate oldDate = today.minusDays(10);
            mockMvc.perform(
                            get("/reports/orders-by-period")
                                    .param("startDate", oldDate.format(DateTimeFormatter.ISO_DATE))
                                    .param("endDate", oldDate.format(DateTimeFormatter.ISO_DATE))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}