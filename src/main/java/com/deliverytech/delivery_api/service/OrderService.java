package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.OrderRequestDto;
import com.deliverytech.delivery_api.dto.response.OrderResponseDto;
import com.deliverytech.delivery_api.dto.response.OrderSummaryResponseDto;
import com.deliverytech.delivery_api.dto.response.OrderTotalResponseDto;
import com.deliverytech.delivery_api.model.Order;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface OrderService {
    OrderResponseDto createOrder(OrderRequestDto dto);
    Order findById(String id);
    OrderResponseDto getOrderResponseById(String id);
    Page<OrderSummaryResponseDto> findByConsumerId(String consumerId, Pageable pageable);
    Page<OrderSummaryResponseDto> findByRestaurantId(String restaurantId, Pageable pageable);
    Page<OrderSummaryResponseDto> searchOrders(OrderStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);
    OrderResponseDto updateOrderStatus(String id, OrderStatus newStatus);
    OrderTotalResponseDto calculateOrderTotal(OrderRequestDto dto);
    void cancelOrder(String orderId);
}
