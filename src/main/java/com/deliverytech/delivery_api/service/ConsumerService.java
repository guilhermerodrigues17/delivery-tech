package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.exceptions.DuplicatedRegisterException;
import com.deliverytech.delivery_api.exceptions.ResourceNotFoundException;
import com.deliverytech.delivery_api.model.Consumer;
import com.deliverytech.delivery_api.repository.ConsumerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final ConsumerRepository consumerRepository;

    public Consumer create(Consumer consumer) {
        var isEmailInUse =
                consumerRepository.findByEmail(consumer.getEmail()).orElseGet(() -> null);
        if (isEmailInUse != null) {
            throw new DuplicatedRegisterException("E-mail já está em uso");
        }

        consumer.setActive(true);
        return consumerRepository.save(consumer);
    }

    public Optional<Consumer> findById(UUID id) {
        return consumerRepository.findById(id);
    }
}
