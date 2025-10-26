package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.request.ConsumerRequestDto;
import com.deliverytech.delivery_api.dto.response.ConsumerResponseDto;
import com.deliverytech.delivery_api.model.Consumer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ConsumerService {
    ConsumerResponseDto create(ConsumerRequestDto dto);
    Consumer findById(UUID id);
    ConsumerResponseDto findByIdResponse(String id);
    ConsumerResponseDto findByEmail(String email);
    Boolean existsByEmail(String email);
    Page<ConsumerResponseDto> findAllActive(Pageable pageable);
    ConsumerResponseDto updateConsumer(String id, ConsumerRequestDto dto);
    void softDeleteConsumer(String id);
}
