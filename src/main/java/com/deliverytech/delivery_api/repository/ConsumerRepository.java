package com.deliverytech.delivery_api.repository;

import com.deliverytech.delivery_api.model.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsumerRepository extends JpaRepository<Consumer, UUID> {
    Optional<Consumer> findByEmail(String email);

    List<Consumer> findByActiveTrue();
}
