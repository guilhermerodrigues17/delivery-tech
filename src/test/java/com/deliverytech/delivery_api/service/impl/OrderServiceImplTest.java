package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.OrderItemRequestDto;
import com.deliverytech.delivery_api.dto.request.OrderRequestDto;
import com.deliverytech.delivery_api.dto.response.OrderResponseDto;
import com.deliverytech.delivery_api.dto.response.OrderSummaryResponseDto;
import com.deliverytech.delivery_api.dto.response.OrderTotalResponseDto;
import com.deliverytech.delivery_api.exceptions.BusinessException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.OrderMapper;
import com.deliverytech.delivery_api.model.*;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import com.deliverytech.delivery_api.repository.OrderRepository;
import com.deliverytech.delivery_api.security.SecurityService;
import com.deliverytech.delivery_api.service.ConsumerService;
import com.deliverytech.delivery_api.service.ProductService;
import com.deliverytech.delivery_api.service.RestaurantService;
import com.deliverytech.delivery_api.validation.OrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ConsumerService consumerService;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private SecurityService securityService;

    @Mock
    private MetricsServiceImpl metricsService;

    @Mock
    private OrderValidator orderValidator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Nested
    @DisplayName("createOrder() tests")
    class CreateOrderTests {
        @Test
        @DisplayName("Should create an order when data is valid")
        void should_CreateOrder_When_DataIsValid() {
            var consumerId = UUID.randomUUID();
            var restaurantId = UUID.randomUUID();
            var productId = UUID.randomUUID();

            OrderItemRequestDto itemDto = new OrderItemRequestDto(productId, 2);
            OrderRequestDto orderRequest = new OrderRequestDto(consumerId, restaurantId, List.of(itemDto));

            Consumer consumerMock = new Consumer();
            consumerMock.setId(consumerId);
            consumerMock.setAddress("Rua A, 100");

            Restaurant restaurantMock = new Restaurant();
            restaurantMock.setId(restaurantId);
            restaurantMock.setDeliveryTax(new BigDecimal("5.00"));

            Product productMock = new Product();
            productMock.setId(productId);
            productMock.setPrice(new BigDecimal("10.00"));
            productMock.setRestaurant(restaurantMock);

            Order savedOrder = new Order();
            savedOrder.setId(UUID.randomUUID());

            OrderResponseDto expectedResponse = new OrderResponseDto(savedOrder.getId(), null, null,
                    null, null, null, null, null, null, null, null);

            when(consumerService.findById(consumerId)).thenReturn(consumerMock);
            when(restaurantService.findById(restaurantId)).thenReturn(restaurantMock);
            when(productService.findProductEntityById(productId.toString())).thenReturn(productMock);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toDto(savedOrder)).thenReturn(expectedResponse);

            OrderResponseDto result = orderService.createOrder(orderRequest);

            assertNotNull(result);
            assertEquals(expectedResponse.id(), result.id());

            ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderArgumentCaptor.capture());
            Order capturedOrder = orderArgumentCaptor.getValue();

            assertEquals(consumerMock, capturedOrder.getConsumer());
            assertEquals(restaurantMock, capturedOrder.getRestaurant());
            assertEquals(consumerMock.getAddress(), capturedOrder.getDeliveryAddress());
            assertEquals(OrderStatus.PENDING, capturedOrder.getStatus());

            assertEquals(restaurantMock.getDeliveryTax(), capturedOrder.getDeliveryTax());
            assertEquals(new BigDecimal("20.00"), capturedOrder.getSubtotal());
            assertEquals(new BigDecimal("25.00"), capturedOrder.getTotal());

            assertEquals(1, capturedOrder.getItems().size());

            OrderItem capturedItem = capturedOrder.getItems().getFirst();
            assertEquals(productMock, capturedItem.getProduct());
            assertEquals(2, capturedItem.getQuantity());
            assertEquals(new BigDecimal("10.00"), capturedItem.getUnitPrice());
            assertEquals(new BigDecimal("20.00"), capturedItem.getSubtotal());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when consumer not exists")
        void should_ThrowResourceNotFound_When_ConsumerNotExists() {
            var consumerId = UUID.randomUUID();
            var restaurantId = UUID.randomUUID();
            OrderRequestDto orderRequest = new OrderRequestDto(consumerId, restaurantId, List.of());

            when(consumerService.findById(consumerId)).thenThrow(new ResourceNotFoundException("Cliente não encontrado"));

            var exception = assertThrows(ResourceNotFoundException.class, () -> {
                orderService.createOrder(orderRequest);
            });

            assertEquals("Cliente não encontrado", exception.getMessage());

            verify(restaurantService, never()).findById(any(UUID.class));
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when restaurant not exists")
        void should_ThrowResourceNotFoundException_When_RestaurantNotExists() {
            var consumerId = UUID.randomUUID();
            var restaurantId = UUID.randomUUID();
            OrderRequestDto orderRequest = new OrderRequestDto(consumerId, restaurantId, List.of());

            Consumer consumer = new Consumer();
            consumer.setId(consumerId);

            when(consumerService.findById(consumerId)).thenReturn(consumer);

            when(restaurantService.findById(restaurantId))
                    .thenThrow(new ResourceNotFoundException("Restaurante não encontrado"));

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                orderService.createOrder(orderRequest);
            });

            assertEquals("Restaurante não encontrado", exception.getMessage());

            verify(consumerService).findById(consumerId);
            verify(productService, never()).findProductEntityById(anyString());
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not exists")
        void should_ThrowResourceNotFoundException_When_ProductNotExists() {
            var consumerId = UUID.randomUUID();
            var restaurantId = UUID.randomUUID();
            var productId = UUID.randomUUID();

            OrderItemRequestDto itemDto = new OrderItemRequestDto(productId, 2);
            OrderRequestDto orderRequest = new OrderRequestDto(consumerId, restaurantId, List.of(itemDto));

            Consumer consumer = new Consumer();
            consumer.setId(consumerId);

            Restaurant restaurant = new Restaurant();
            restaurant.setId(restaurantId);

            when(consumerService.findById(consumerId)).thenReturn(consumer);
            when(restaurantService.findById(restaurantId)).thenReturn(restaurant);
            when(productService.findProductEntityById(productId.toString()))
                    .thenThrow(new ResourceNotFoundException("Produto não encontrado"));

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                orderService.createOrder(orderRequest);
            });

            assertEquals("Produto não encontrado", exception.getMessage());

            verify(consumerService).findById(consumerId);
            verify(restaurantService).findById(restaurantId);
            verify(productService).findProductEntityById(productId.toString());
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when product not belong to restaurant")
        void should_ThrowBusinessException_When_ProductNotBelongToRestaurant() {
            var consumerId = UUID.randomUUID();
            var restaurantId = UUID.randomUUID();
            var productId = UUID.randomUUID();

            OrderItemRequestDto itemDto = new OrderItemRequestDto(productId, 2);
            OrderRequestDto orderRequest = new OrderRequestDto(consumerId, restaurantId, List.of(itemDto));

            Consumer consumer = new Consumer();
            consumer.setId(consumerId);

            Restaurant restaurant1 = new Restaurant();
            restaurant1.setId(restaurantId);
            restaurant1.setDeliveryTax(new BigDecimal("10.00"));

            Restaurant restaurant2 = new Restaurant();
            restaurant2.setId(UUID.randomUUID());

            Product product = new Product();
            product.setId(productId);
            product.setName("Product");
            product.setRestaurant(restaurant2);
            product.setPrice(new BigDecimal("10.00"));

            when(consumerService.findById(consumerId)).thenReturn(consumer);
            when(restaurantService.findById(restaurantId)).thenReturn(restaurant1);
            when(productService.findProductEntityById(productId.toString())).thenReturn(product);

            String expectedMessage = String.format("O produto '%s' (%s) não pertence ao restaurante informado.",
                    product.getName(), product.getId());
            BusinessException businessException = new BusinessException(expectedMessage);

            doThrow(businessException).when(orderValidator).validateProductBelongsRestaurant(restaurant1, product);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                orderService.createOrder(orderRequest);
            });

            assertEquals(expectedMessage, exception.getMessage());

            verify(orderRepository, never()).save(any(Order.class));
            verify(orderMapper, never()).toDto(any(Order.class));
        }
    }

    @Nested
    @DisplayName("updateOrderStatus() tests")
    class UpdateOrderStatusTests {
        @Test
        @DisplayName("Should update OrderStatus when new OrderStatus is valid")
        void should_UpdateOrderStatus_When_NewOrderStatusIsValid() {
            var orderId = UUID.randomUUID();
            var newOrderStatus = OrderStatus.PREPARING;

            Order order = new Order();
            order.setId(orderId);
            order.setStatus(OrderStatus.PENDING);

            OrderResponseDto expectedResponse = new OrderResponseDto(orderId, null, null, null,
                    newOrderStatus, null, null, null, null, null, null);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
            when(orderMapper.toDto(any(Order.class))).thenReturn(expectedResponse);

            OrderResponseDto result = orderService.updateOrderStatus(orderId.toString(), newOrderStatus);

            assertNotNull(result);
            assertEquals(expectedResponse.status(), result.status());

            ArgumentCaptor<Order> orderArgumentCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderArgumentCaptor.capture());

            Order capturedOrder = orderArgumentCaptor.getValue();
            assertEquals(newOrderStatus, capturedOrder.getStatus());

            verify(orderMapper).toDto(capturedOrder);
        }

        @Test
        @DisplayName("Should throw BusinessException when transition is invalid")
        void should_ThrowBusinessException_When_TransitionIsInvalid() {
            var orderId = UUID.randomUUID();
            OrderStatus currentStatus = OrderStatus.DELIVERED;
            OrderStatus newStatus = OrderStatus.CANCELED;

            Order order = new Order();
            order.setId(orderId);
            order.setStatus(currentStatus);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                orderService.updateOrderStatus(orderId.toString(), newStatus);
            });

            String expectedMessage = "Não é possível mudar de " + currentStatus + " para " + newStatus;
            assertEquals(expectedMessage, exception.getMessage());

            verify(orderRepository, never()).save(any(Order.class));
            verify(orderMapper, never()).toDto(any(Order.class));
        }
    }

    @Nested
    @DisplayName("findByRestaurantId() tests")
    class FindByRestaurantIdTests {

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
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when restaurantId is not a valid UUID")
        void should_ThrowIllegalArgumentException_When_IdIsInvalidUUID() {
            String invalidUuidString = "not-a-uuid";

            assertThrows(IllegalArgumentException.class, () -> {
                orderService.findByRestaurantId(invalidUuidString, pageable);
            });

            verify(restaurantService, never()).findById(any(UUID.class));
            verify(orderRepository, never()).findByRestaurantId(any(UUID.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when restaurantId does not exist")
        void should_ThrowResourceNotFound_When_RestaurantNotFound() {
            when(restaurantService.findById(restaurantId)).thenThrow(new ResourceNotFoundException("Restaurante não encontrado"));

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                orderService.findByRestaurantId(restaurantIdString, pageable);
            });

            assertEquals("Restaurante não encontrado", exception.getMessage());

            verify(restaurantService).findById(restaurantId);
            verify(orderRepository, never()).findByRestaurantId(any(UUID.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty Page when restaurant has no orders")
        void should_ReturnEmptyPage_When_RestaurantHasNoOrders() {
            when(restaurantService.findById(restaurantId)).thenReturn(mockRestaurant);

            Page<Order> emptyPage = Page.empty(pageable);
            when(orderRepository.findByRestaurantId(restaurantId, pageable)).thenReturn(emptyPage);

            Page<OrderSummaryResponseDto> resultPage = orderService.findByRestaurantId(restaurantIdString, pageable);

            assertNotNull(resultPage);
            assertTrue(resultPage.isEmpty());
            assertEquals(0, resultPage.getTotalElements());

            verify(restaurantService).findById(restaurantId);
            verify(orderRepository).findByRestaurantId(restaurantId, pageable);
            verify(orderMapper, never()).toSummaryDto(any(Order.class));
        }

        @Test
        @DisplayName("Should return paged DTOs when restaurant has orders")
        void should_ReturnDtoPage_When_RestaurantHasOrders() {
            Order mockOrder = new Order();
            mockOrder.setId(UUID.randomUUID());
            mockOrder.setRestaurant(mockRestaurant);

            List<Order> orderList = List.of(mockOrder);
            Page<Order> mockRepoPage = new PageImpl<>(orderList, pageable, 1);

            OrderSummaryResponseDto expectedDto = new OrderSummaryResponseDto(
                    mockOrder.getId(), "Test Restaurant", OrderStatus.PENDING, BigDecimal.TEN
            );

            when(restaurantService.findById(restaurantId)).thenReturn(mockRestaurant);
            when(orderRepository.findByRestaurantId(restaurantId, pageable)).thenReturn(mockRepoPage);
            when(orderMapper.toSummaryDto(mockOrder)).thenReturn(expectedDto);

            Page<OrderSummaryResponseDto> resultPage = orderService.findByRestaurantId(restaurantIdString, pageable);

            assertNotNull(resultPage);
            assertEquals(1, resultPage.getTotalElements());
            assertEquals(1, resultPage.getContent().size());
            assertEquals(expectedDto, resultPage.getContent().getFirst());

            verify(restaurantService).findById(restaurantId);
            verify(orderRepository).findByRestaurantId(restaurantId, pageable);
            verify(orderMapper).toSummaryDto(mockOrder);
        }
    }

    @Nested
    @DisplayName("searchOrders() tests")
    class SearchOrdersTests {

        private Pageable pageable;
        private LocalDate startDate;
        private LocalDate endDate;

        @BeforeEach
        void setUp() {
            pageable = PageRequest.of(0, 5);
            startDate = LocalDate.of(2025, 1, 1);
            endDate = LocalDate.of(2025, 1, 31);
        }

        @Test
        @DisplayName("Should call findAll with Specification and map results when all filters are provided")
        void should_CallFindAllAndMap_When_AllFiltersProvided() {
            Order mockOrder = new Order();
            mockOrder.setId(UUID.randomUUID());
            List<Order> orderList = List.of(mockOrder);
            Page<Order> mockRepoPage = new PageImpl<>(orderList, pageable, 1);

            OrderSummaryResponseDto expectedDto = new OrderSummaryResponseDto(
                    mockOrder.getId(), "Test Restaurant", OrderStatus.DELIVERED, BigDecimal.TEN
            );

            when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockRepoPage);
            when(orderMapper.toSummaryDto(mockOrder)).thenReturn(expectedDto);

            Page<OrderSummaryResponseDto> resultPage = orderService
                    .searchOrders(OrderStatus.DELIVERED, startDate, endDate, pageable);

            assertNotNull(resultPage);
            assertEquals(1, resultPage.getTotalElements());
            assertEquals(expectedDto, resultPage.getContent().getFirst());

            verify(orderRepository).findAll(any(Specification.class), eq(pageable));
            verify(orderMapper).toSummaryDto(mockOrder);
        }

        @Test
        @DisplayName("Should call findAll with Specification when filters are null")
        void should_CallFindAll_When_FiltersAreNull() {
            Page<Order> emptyPage = Page.empty(pageable);

            when(orderRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(emptyPage);

            Page<OrderSummaryResponseDto> resultPage = orderService.searchOrders(null, null, null, pageable);

            assertNotNull(resultPage);
            assertTrue(resultPage.isEmpty());

            verify(orderRepository).findAll(any(Specification.class), eq(pageable));
            verify(orderMapper, never()).toSummaryDto(any(Order.class));
        }

        @Test
        @DisplayName("Should correctly handle logic when only startDate is provided")
        void should_HandleLogic_When_OnlyStartDateIsProvided() {
            Page<Order> emptyPage = Page.empty(pageable);

            when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            Page<OrderSummaryResponseDto> resultPage = orderService
                    .searchOrders(OrderStatus.PENDING, startDate, null, pageable);

            assertNotNull(resultPage);
            assertTrue(resultPage.isEmpty());

            verify(orderRepository).findAll(any(Specification.class), eq(pageable));
            verify(orderMapper, never()).toSummaryDto(any(Order.class));
        }
    }

    @Nested
    @DisplayName("calculateOrderTotal() tests")
    class CalculateOrderTotalTests {

        private OrderRequestDto requestDto;
        private UUID restaurantId;
        private UUID productId;
        private Restaurant mockRestaurant;
        private Product mockProduct;

        @BeforeEach
        void setUp() {
            restaurantId = UUID.randomUUID();
            productId = UUID.randomUUID();

            OrderItemRequestDto itemDto = new OrderItemRequestDto(productId, 2);
            List<OrderItemRequestDto> items = new ArrayList<>();
            items.add(itemDto);
            requestDto = new OrderRequestDto(UUID.randomUUID(), restaurantId, items);

            mockRestaurant = new Restaurant();
            mockRestaurant.setId(restaurantId);
            mockRestaurant.setDeliveryTax(new BigDecimal("8.00"));

            mockProduct = new Product();
            mockProduct.setId(productId);
            mockProduct.setPrice(new BigDecimal("10.00"));
            mockProduct.setRestaurant(mockRestaurant);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when restaurant does not exist")
        void should_ThrowResourceNotFound_When_RestaurantNotFound() {
            when(restaurantService.findById(restaurantId)).thenThrow(new ResourceNotFoundException("Restaurante não encontrado"));

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                orderService.calculateOrderTotal(requestDto);
            });

            assertEquals("Restaurante não encontrado", exception.getMessage());

            verify(restaurantService).findById(restaurantId);
            verify(productService, never()).findProductEntityById(anyString());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product does not exist")
        void should_ThrowResourceNotFound_When_ProductNotFound() {
            when(restaurantService.findById(restaurantId)).thenReturn(mockRestaurant);
            when(productService.findProductEntityById(productId.toString()))
                    .thenThrow(new ResourceNotFoundException("Produto não encontrado"));

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                orderService.calculateOrderTotal(requestDto);
            });

            assertEquals("Produto não encontrado", exception.getMessage());

            verify(restaurantService).findById(restaurantId);
            verify(productService).findProductEntityById(productId.toString());
        }

        @Test
        @DisplayName("Should throw BusinessException when product does not belong to restaurant")
        void should_ThrowBusinessException_When_ProductDoesNotBelongToRestaurant() {
            Restaurant otherRestaurant = new Restaurant();
            otherRestaurant.setId(UUID.randomUUID());
            mockProduct.setRestaurant(otherRestaurant);

            when(restaurantService.findById(restaurantId)).thenReturn(mockRestaurant);
            when(productService.findProductEntityById(productId.toString())).thenReturn(mockProduct);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                orderService.calculateOrderTotal(requestDto);
            });

            assertTrue(exception.getMessage().contains("não pertence ao restaurante informado"));

            verify(restaurantService).findById(restaurantId);
            verify(productService).findProductEntityById(productId.toString());
        }

        @Test
        @DisplayName("Should return correct totals when data is valid")
        void should_ReturnCorrectTotals_When_DataIsValid() {
            UUID productId2 = UUID.randomUUID();
            Product mockProduct2 = new Product();
            mockProduct2.setId(productId2);
            mockProduct2.setPrice(new BigDecimal("5.00"));
            mockProduct2.setRestaurant(mockRestaurant);

            requestDto.getItems().add(new OrderItemRequestDto(productId2, 1));

            when(restaurantService.findById(restaurantId)).thenReturn(mockRestaurant);
            when(productService.findProductEntityById(productId.toString())).thenReturn(mockProduct);
            when(productService.findProductEntityById(productId2.toString())).thenReturn(mockProduct2);

            OrderTotalResponseDto response = orderService.calculateOrderTotal(requestDto);

            assertNotNull(response);

            assertEquals(0, response.subtotal().compareTo(new BigDecimal("25.00")));
            assertEquals(0, response.deliveryTax().compareTo(new BigDecimal("8.00")));
            assertEquals(0, response.total().compareTo(new BigDecimal("33.00")));

            verify(restaurantService).findById(restaurantId);
            verify(productService).findProductEntityById(productId.toString());
            verify(productService).findProductEntityById(productId2.toString());
        }
    }

    @Nested
    @DisplayName("cancelOrder() tests")
    class CancelOrderTests {

        private UUID orderId;
        private String orderIdString;
        private Order mockOrder;

        @BeforeEach
        void setUp() {
            orderId = UUID.randomUUID();
            orderIdString = orderId.toString();

            mockOrder = new Order();
            mockOrder.setId(orderId);
            mockOrder.setRestaurant(new Restaurant());
            mockOrder.setConsumer(new Consumer());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when orderId is not a valid UUID")
        void should_ThrowIllegalArgumentException_When_IdIsInvalidUUID() {
            String invalidUuidString = "not-a-uuid";

            assertThrows(IllegalArgumentException.class, () -> {
                orderService.cancelOrder(invalidUuidString);
            });

            verify(orderRepository, never()).findById(any(UUID.class));
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when orderId does not exist")
        void should_ThrowResourceNotFound_When_OrderNotFound() {
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                orderService.cancelOrder(orderIdString);
            });

            verify(orderRepository).findById(orderId);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when order status cannot transition to CANCELED")
        void should_ThrowBusinessException_When_TransitionIsInvalid() {
            mockOrder.setStatus(OrderStatus.DELIVERED);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                orderService.cancelOrder(orderIdString);
            });

            String expectedMessage = String.format("Não é possível cancelar o pedido com status '%s'.", OrderStatus.DELIVERED);
            assertEquals(expectedMessage, exception.getMessage());

            verify(orderRepository).findById(orderId);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should update status to CANCELED when transition is valid")
        void should_UpdateStatusToCanceled_When_TransitionIsValid() {
            mockOrder.setStatus(OrderStatus.PENDING);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(null);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

            assertDoesNotThrow(() -> {
                orderService.cancelOrder(orderIdString);
            });

            verify(orderRepository).findById(orderId);
            verify(orderRepository).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();
            assertEquals(OrderStatus.CANCELED, savedOrder.getStatus());
        }
    }
}
