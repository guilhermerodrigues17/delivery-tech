package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.OrderRequestDto;
import com.deliverytech.delivery_api.dto.response.OrderResponseDto;
import com.deliverytech.delivery_api.dto.response.OrderSummaryResponseDto;
import com.deliverytech.delivery_api.dto.response.OrderTotalResponseDto;
import com.deliverytech.delivery_api.model.Order;
import com.deliverytech.delivery_api.model.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderRequestDto dto);
    OrderResponseDto createOrderResponse(OrderRequestDto dto);
    Order findById(String id);
    OrderResponseDto getOrderResponseById(String id);
    List<Order> findByConsumerId(String consumerId);
    List<OrderSummaryResponseDto> findByConsumerIdResponse(String consumerId);
    OrderResponseDto updateOrderStatus(String id, OrderStatus newStatus);
    OrderTotalResponseDto calculateOrderTotal(OrderRequestDto dto);
    void cancelOrder(String orderId);
}
