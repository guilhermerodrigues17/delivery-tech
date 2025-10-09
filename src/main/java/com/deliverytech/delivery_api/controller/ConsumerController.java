package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.ConsumerRequestDto;
import com.deliverytech.delivery_api.dto.response.ConsumerResponseDto;
import com.deliverytech.delivery_api.mapper.ConsumerMapper;
import com.deliverytech.delivery_api.model.Consumer;
import com.deliverytech.delivery_api.service.ConsumerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consumers")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerService consumerService;
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

    @GetMapping()
    public ResponseEntity<List<ConsumerResponseDto>> findAllActive() {
        List<Consumer> consumers = consumerService.findAllActive();
        var response = consumers.stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsumerResponseDto> updateConsumer(@PathVariable String id,
            @Valid @RequestBody ConsumerRequestDto dto) {
        var response = consumerService.updateConsumer(id, dto);
        return ResponseEntity.ok(response);

    }
}
