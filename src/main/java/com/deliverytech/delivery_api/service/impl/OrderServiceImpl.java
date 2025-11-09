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
import com.deliverytech.delivery_api.repository.specification.OrderSpecification;
import com.deliverytech.delivery_api.security.SecurityService;
import com.deliverytech.delivery_api.service.ConsumerService;
import com.deliverytech.delivery_api.service.OrderService;
import com.deliverytech.delivery_api.service.ProductService;
import com.deliverytech.delivery_api.service.RestaurantService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("orderServiceImpl")
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ConsumerService consumerService;
    private final RestaurantService restaurantService;
    private final ProductService productService;
    private final OrderMapper orderMapper;
    private final SecurityService securityService;
    private final MetricsServiceImpl metricsService;

    @Transactional
    @Timed("delivery_api.orders.creation.timer")
    public OrderResponseDto createOrder(OrderRequestDto dto) {
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
                throw new BusinessException(
                        String.format("O produto '%s' (%s) não pertence ao restaurante informado.",
                                product.getName(), product.getId()));
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

        var savedOrder = orderRepository.save(order);
        metricsService.incrementOrdersProcessed(savedOrder);

        return orderMapper.toDto(savedOrder);
    }

    public Order findById(String id) {
        return orderRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado "));
    }

    @Transactional(readOnly = true)
    @Timed("delivery_api.orders.findById.timer")
    public OrderResponseDto getOrderResponseById(String id) {
        Order order = findById(id);
        return orderMapper.toDto(order);
    }

    @Timed("delivery_api.orders.findByConsumerId.timer")
    public Page<OrderSummaryResponseDto> findByConsumerId(String consumerId, Pageable pageable) {
        Consumer consumer = consumerService.findById(UUID.fromString(consumerId));
        Page<Order> orderPages = orderRepository.findByConsumerId(consumer.getId(), pageable);
        return orderPages.map(orderMapper::toSummaryDto);
    }

    @Override
    @Timed("delivery_api.orders.findByRestaurantId.timer")
    public Page<OrderSummaryResponseDto> findByRestaurantId(String restaurantId, Pageable pageable) {
        var restaurant = restaurantService.findById(UUID.fromString(restaurantId));
        Page<Order> ordersPage = orderRepository.findByRestaurantId(restaurant.getId(), pageable);
        return ordersPage.map(orderMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OrderSummaryResponseDto> searchOrders(OrderStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null) startDateTime = startDate.atStartOfDay();
        if (endDate != null) endDateTime = endDate.atTime(LocalTime.MAX);
        if (startDate != null && endDate == null) endDateTime = startDate.atTime(LocalTime.MAX);

        Specification<Order> spec = Specification.allOf(
                OrderSpecification.withStatus(status),
                OrderSpecification.withStartDate(startDateTime),
                OrderSpecification.withEndDate(endDateTime));

        Page<Order> ordersPage = orderRepository.findAll(spec, pageable);
        return ordersPage.map(orderMapper::toSummaryDto);
    }

    @Transactional
    @Timed("delivery_api.orders.updateStatus.timer")
    public OrderResponseDto updateOrderStatus(String id, OrderStatus newStatus) {
        Order order = findById(id);
        var currentStatus = order.getStatus();

        if (!currentStatus.canTransition(newStatus)) {
            throw new BusinessException(
                    "Não é possível mudar de " + currentStatus + " para " + newStatus);
        }

        order.setStatus(newStatus);
        var updatedOrder = orderRepository.save(order);

        metricsService.incrementOrdersDelivered(updatedOrder);
        metricsService.incrementOrdersCanceled(updatedOrder);

        return orderMapper.toDto(updatedOrder);
    }

    public OrderTotalResponseDto calculateOrderTotal(OrderRequestDto dto) {
        var restaurant = restaurantService.findById(dto.getRestaurantId());
        BigDecimal subtotal = calculateSubtotal(dto.getItems(), restaurant.getId());
        BigDecimal deliveryTax = restaurant.getDeliveryTax();
        BigDecimal totalPrice = subtotal.add(deliveryTax);

        return new OrderTotalResponseDto(subtotal, deliveryTax, totalPrice);
    }

    @Transactional
    public void cancelOrder(String orderId) {
        Order order = findById(orderId);

        if (!order.getStatus().canTransition(OrderStatus.CANCELED)) {
            throw new BusinessException(
                    String.format("Não é possível cancelar o pedido com status '%s'.", order.getStatus())
            );
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    public boolean isOwnerConsumer(String orderId) {
        Optional<String> userEmail = securityService.getCurrentUser().map(User::getEmail);
        if (userEmail.isEmpty()) return false;

        Order order = findById(orderId);
        return order.getConsumer().getEmail().equalsIgnoreCase(userEmail.get());
    }

    public boolean isOwnerRestaurant(String orderId) {
        Optional<UUID> currentUserRestaurantId = securityService.getCurrentUserRestaurantId();
        if (currentUserRestaurantId.isEmpty()) return false;

        Order order = findById(orderId);
        return order.getRestaurant().getId().equals(currentUserRestaurantId.get());
    }

    private BigDecimal calculateSubtotal(List<OrderItemRequestDto> items, UUID restaurantId) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequestDto itemDto : items) {
            Product product = productService.findProductEntityById(itemDto.getProductId().toString());
            if (!product.getRestaurant().getId().equals(restaurantId)) {
                throw new BusinessException(
                        String.format("O produto '%s' (%s) não pertence ao restaurante informado.", product.getName(), product.getId())
                );
            }

            BigDecimal itemPrice = product.getPrice().multiply(new BigDecimal(itemDto.getQuantity()));
            subtotal = subtotal.add(itemPrice);
        }

        return subtotal;
    }

    private LocalDate parseStringDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(date, formatter);
    }
}
