package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.OrderRequestDto;
import com.deliverytech.delivery_api.dto.request.OrderStatusUpdateRequestDto;
import com.deliverytech.delivery_api.dto.response.OrderResponseDto;
import com.deliverytech.delivery_api.dto.response.OrderTotalResponseDto;
import com.deliverytech.delivery_api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto dto) {
        var response = orderService.createOrderResponse(dto);
        return ResponseEntity.created(null).body(response);
    }

    @PostMapping("/calculate-total")
    public ResponseEntity<OrderTotalResponseDto> calculateOrderTotal(@Valid @RequestBody OrderRequestDto dto) {
        var response = orderService.calculateOrderTotal(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> findOrderById(@PathVariable String id) {
        var response = orderService.getOrderResponseById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping(params = "consumerId")
    public ResponseEntity<List<OrderResponseDto>> findOrdersByConsumerId(
            @RequestParam String consumerId) {
        var ordersResponse = orderService.findByConsumerIdResponse(consumerId);
        return ResponseEntity.ok(ordersResponse);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable String id,
                                                              @RequestBody OrderStatusUpdateRequestDto dto) {
        var response = orderService.updateOrderStatus(id, dto.getStatus());
        return ResponseEntity.ok(response);
    }
}
