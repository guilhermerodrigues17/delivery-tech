package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.OrderRequestDto;
import com.deliverytech.delivery_api.dto.response.OrderResponseDto;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.OrderMapper;
import com.deliverytech.delivery_api.model.*;
import com.deliverytech.delivery_api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ConsumerService consumerService;
    private final RestaurantService restaurantService;
    private final ProductService productService;
    private final OrderMapper orderMapper;

    @Transactional
    public Order createOrder(OrderRequestDto dto) {
        Consumer consumer = consumerService.findById(dto.getConsumerId());
        Restaurant restaurant = restaurantService.findById(dto.getRestaurantId());

        Order order = new Order();
        order.setConsumer(consumer);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(consumer.getAddress());
        order.setDeliveryTax(restaurant.getDeliveryTax());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = dto.getItems().stream().map(item -> {
            Product product = productService.findProductEntityById(item.getProductId().toString());

            if (!product.getRestaurant().getId().equals(restaurant.getId())) {
                throw new IllegalArgumentException("O produto " + product.getName()
                        + " não pertence ao restaurante selecionado.");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubtotal(
                    product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItem.setOrder(order);
            return orderItem;

        }).toList();

        order.setItems(orderItems);

        BigDecimal subtotal = orderItems.stream().map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(order.getDeliveryTax()));

        return orderRepository.save(order);
    }

    @Transactional
    public OrderResponseDto createOrderResponse(OrderRequestDto dto) {
        Order order = createOrder(dto);
        return orderMapper.toDto(order);
    }

    public Order findById(String id) {
        return orderRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado "));
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderResponseById(String id) {
        Order order = findById(id);
        return orderMapper.toDto(order);
    }

    public List<Order> findByConsumerId(String consumerId) {
        return orderRepository.findByConsumerId(UUID.fromString(consumerId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> findByConsumerIdResponse(String consumerId) {
        List<Order> orders = findByConsumerId(consumerId);
        return orders.stream().map(orderMapper::toDto).toList();
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(String id, OrderStatus newStatus) {
        Order order = findById(id);
        var currentStatus = order.getStatus();

        if (!currentStatus.canTransition(newStatus)) {
            throw new IllegalStateException(
                    "Não é possível mudar de " + currentStatus + " para " + newStatus);
        }

        order.setStatus(newStatus);
        var response = orderRepository.save(order);

        return orderMapper.toDto(response);
    }
}
