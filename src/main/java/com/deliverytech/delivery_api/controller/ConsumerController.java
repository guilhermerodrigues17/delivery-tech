package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.ConsumerRequestDto;
import com.deliverytech.delivery_api.dto.response.ConsumerResponseDto;
import com.deliverytech.delivery_api.dto.response.OrderSummaryResponseDto;
import com.deliverytech.delivery_api.mapper.ConsumerMapper;
import com.deliverytech.delivery_api.model.Consumer;
import com.deliverytech.delivery_api.service.ConsumerService;
import com.deliverytech.delivery_api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/consumers")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerService consumerService;
    private final OrderService orderService;
    private final ConsumerMapper mapper;

    @PostMapping
    public ResponseEntity<ConsumerResponseDto> create(@Valid @RequestBody ConsumerRequestDto dto) {
        Consumer consumer = mapper.toEntity(dto);
        Consumer consumerCreated = consumerService.create(consumer);
        var response = mapper.toDto(consumerCreated);
        return ResponseEntity.created(null).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsumerResponseDto> findById(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        Consumer consumer = consumerService.findById(uuid);
        var response = mapper.toDto(consumer);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ConsumerResponseDto>> findAllActive() {
        List<Consumer> consumers = consumerService.findAllActive();
        var response = consumers.stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping(params = "email")
    public ResponseEntity<ConsumerResponseDto> findConsumerByEmail(@RequestParam String email) {
        var consumer = consumerService.findByEmail(email);
        var response = mapper.toDto(consumer);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{consumerId}/orders")
    public ResponseEntity<List<OrderSummaryResponseDto>> findOrdersByConsumerId(@PathVariable String consumerId) {
        var ordersResponse = orderService.findByConsumerIdResponse(consumerId);
        return ResponseEntity.ok(ordersResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsumerResponseDto> updateConsumer(@PathVariable String id,
                                                              @Valid @RequestBody ConsumerRequestDto dto) {
        var response = consumerService.updateConsumer(id, dto);
        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteConsumer(@PathVariable String id) {
        consumerService.softDeleteConsumer(id);
        return ResponseEntity.noContent().build();
    }
}
