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
import com.deliverytech.delivery_api.service.ConsumerService;
import com.deliverytech.delivery_api.service.OrderService;
import com.deliverytech.delivery_api.service.ProductService;
import com.deliverytech.delivery_api.service.RestaurantService;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ConsumerService consumerService;
    private final RestaurantService restaurantService;
    private final ProductService productService;
    private final OrderMapper orderMapper;

    @Transactional
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

        var saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
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
        Consumer consumer = consumerService.findById(UUID.fromString(consumerId));
        return orderRepository.findByConsumerId(consumer.getId());
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponseDto> findByConsumerIdResponse(String consumerId) {
        List<Order> orders = findByConsumerId(consumerId);
        return orderMapper.toSummaryDtoList(orders);
    }

    @Override
    public List<OrderSummaryResponseDto> findByRestaurantId(String restaurantId) {
        var restaurant = restaurantService.findById(UUID.fromString(restaurantId));
        List<Order> orders = orderRepository.findByRestaurantId(restaurant.getId());
        return orderMapper.toSummaryDtoList(orders);
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
    public OrderResponseDto updateOrderStatus(String id, OrderStatus newStatus) {
        Order order = findById(id);
        var currentStatus = order.getStatus();

        if (!currentStatus.canTransition(newStatus)) {
            throw new BusinessException(
                    "Não é possível mudar de " + currentStatus + " para " + newStatus);
        }

        order.setStatus(newStatus);
        var response = orderRepository.save(order);

        return orderMapper.toDto(response);
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
