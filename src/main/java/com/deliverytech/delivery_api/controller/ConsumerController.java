package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.controller.dto.ConsumerRequestDto;
import com.deliverytech.delivery_api.controller.dto.ConsumerResponseDto;
import com.deliverytech.delivery_api.controller.mapper.ConsumerMapper;
import com.deliverytech.delivery_api.model.Consumer;
import com.deliverytech.delivery_api.service.ConsumerService;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerService consumerService;
    private final ConsumerMapper mapper;

    @PostMapping
    public ResponseEntity<ConsumerResponseDto> create(@RequestBody ConsumerRequestDto dto) {
        Consumer consumer = mapper.toEntity(dto);
        Consumer consumerCreated = consumerService.create(consumer);
        var response = mapper.toDto(consumerCreated);
        return ResponseEntity.ok(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<ConsumerResponseDto> findById(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        return consumerService.findById(uuid).map(consumer -> {
            var response = mapper.toDto(consumer);
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());

    }
}
