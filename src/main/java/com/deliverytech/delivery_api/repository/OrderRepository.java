package com.deliverytech.delivery_api.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.deliverytech.delivery_api.model.Order;
import com.deliverytech.delivery_api.model.OrderStatus;
import java.time.LocalDateTime;


public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByConsumerId(UUID consumerId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findTop10ByOrderByOrderDateDesc();
}
