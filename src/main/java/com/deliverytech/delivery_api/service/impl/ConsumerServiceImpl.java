package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.request.ConsumerRequestDto;
import com.deliverytech.delivery_api.dto.response.ConsumerResponseDto;
import com.deliverytech.delivery_api.exceptions.ConflictException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.mapper.ConsumerMapper;
import com.deliverytech.delivery_api.model.Consumer;
import com.deliverytech.delivery_api.model.User;
import com.deliverytech.delivery_api.repository.ConsumerRepository;
import com.deliverytech.delivery_api.security.SecurityService;
import com.deliverytech.delivery_api.service.ConsumerService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service("consumerServiceImpl")
@RequiredArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {

    private final ConsumerRepository consumerRepository;
    private final ConsumerMapper mapper;
    private final SecurityService securityService;

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Timed("delivery_api.consumers.creation.timer")
    public ConsumerResponseDto create(ConsumerRequestDto dto) {
        if (existsByEmail(dto.getEmail())) {
            throw new ConflictException("E-mail já está em uso");
        }

        Consumer consumerEntity = mapper.toEntity(dto);
        consumerEntity.setActive(true);

        String rawPhone = dto.getPhoneNumber();
        if (rawPhone != null) {
            var trimmedPhone = rawPhone.replaceAll("\\D", "");
            consumerEntity.setPhoneNumber(trimmedPhone);
        }

        var savedConsumer = consumerRepository.save(consumerEntity);

        var currentUserOpt = securityService.getCurrentUser();
        String currentUser = "ANONYMOUS";
        if (currentUserOpt.isPresent()) {
            currentUser = currentUserOpt.get().getEmail();
        }

        auditLogger.info("CRUD_EVENT; type=CREATE; entity=Consumer; entityId={}; user={}; correlationId={}",
                savedConsumer.getId(),
                currentUser,
                MDC.get("correlationId")
        );

        return mapper.toDto(savedConsumer);
    }

    public Consumer findById(UUID id) {
        return consumerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }

    @Transactional(readOnly = true)
    public ConsumerResponseDto findByIdResponse(String id) {
        Consumer consumer = findById(UUID.fromString(id));
        return mapper.toDto(consumer);
    }

    @Transactional(readOnly = true)
    public ConsumerResponseDto findByEmail(String email) {
        var consumer = consumerRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        return mapper.toDto(consumer);
    }

    public Boolean existsByEmail(String email) {
        return consumerRepository.existsByEmail(email);
    }

    public Page<ConsumerResponseDto> findAllActive(Pageable pageable) {
        Page<Consumer> consumerPage = consumerRepository.findByActiveTrue(pageable);
        return consumerPage.map(mapper::toDto);
    }

    public ConsumerResponseDto updateConsumer(String id, ConsumerRequestDto dto) {
        Consumer existingConsumer = findById(UUID.fromString(id));

        if (!existingConsumer.getEmail().equals(dto.getEmail())) {

            if (existsByEmail(dto.getEmail())) {
                throw new ConflictException("E-mail já está em uso");
            }

            existingConsumer.setEmail(dto.getEmail());
        }

        existingConsumer.setName(dto.getName());
        existingConsumer.setAddress(dto.getAddress());

        String rawPhone = dto.getPhoneNumber();
        if (rawPhone != null) {
            var trimmedPhone = rawPhone.replaceAll("\\D", "");
            existingConsumer.setPhoneNumber(trimmedPhone);
        }

        var updatedConsumer = consumerRepository.save(existingConsumer);

        var currentUserOpt = securityService.getCurrentUser();
        String currentUser = "ANONYMOUS";
        if (currentUserOpt.isPresent()) {
            currentUser = currentUserOpt.get().getEmail();
        }

        auditLogger.info("CRUD_EVENT; type=UPDATE; entity=Consumer; entityId={}; user={}; correlationId={}",
                updatedConsumer.getId(),
                currentUser,
                MDC.get("correlationId")
        );

        return mapper.toDto(updatedConsumer);
    }

    public void softDeleteConsumer(String id) {
        Consumer existingConsumer = findById(UUID.fromString(id));
        existingConsumer.setActive(false);

        consumerRepository.save(existingConsumer);

        var currentUserOpt = securityService.getCurrentUser();
        String currentUser = "ANONYMOUS";
        if (currentUserOpt.isPresent()) {
            currentUser = currentUserOpt.get().getEmail();
        }

        auditLogger.info("CRUD_EVENT; type=DELETE; entity=Consumer; entityId={}; user={}; correlationId={}",
                existingConsumer.getId(),
                currentUser,
                MDC.get("correlationId")
        );

    }

    public boolean isOwnerByEmail(String consumerId) {
        Optional<String> currentUserEmail = securityService.getCurrentUser().map(User::getEmail);
        if (currentUserEmail.isEmpty()) {
            return false;
        }

        Consumer consumer = findById(UUID.fromString(consumerId));
        return consumer.getEmail().equalsIgnoreCase(currentUserEmail.get());
    }
}
