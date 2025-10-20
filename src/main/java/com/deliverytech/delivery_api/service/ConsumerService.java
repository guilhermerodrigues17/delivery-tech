package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.ConsumerRequestDto;
import com.deliverytech.delivery_api.dto.response.ConsumerResponseDto;
import com.deliverytech.delivery_api.model.Consumer;

import java.util.List;
import java.util.UUID;

public interface ConsumerService {
    Consumer create(Consumer consumer);
    Consumer findById(UUID id);
    Consumer findByEmail(String email);
    Boolean existsByEmail(String email);
    List<Consumer> findAllActive();
    ConsumerResponseDto updateConsumer(String id, ConsumerRequestDto dto);
    void softDeleteConsumer(String id);
}
