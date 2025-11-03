package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.OrderItemRequestDto;
import com.deliverytech.delivery_api.dto.request.OrderRequestDto;
import com.deliverytech.delivery_api.dto.response.OrderResponseDto;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

            Restaurant restaurant2 = new Restaurant();
            restaurant2.setId(UUID.randomUUID());

            Product product = new Product();
            product.setId(productId);
            product.setRestaurant(restaurant2);

            when(consumerService.findById(consumerId)).thenReturn(consumer);
            when(restaurantService.findById(restaurantId)).thenReturn(restaurant1);
            when(productService.findProductEntityById(productId.toString())).thenReturn(product);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                orderService.createOrder(orderRequest);
            });

            String expectedMessage = String.format("O produto '%s' (%s) não pertence ao restaurante informado.",
                    product.getName(), product.getId());
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
}
