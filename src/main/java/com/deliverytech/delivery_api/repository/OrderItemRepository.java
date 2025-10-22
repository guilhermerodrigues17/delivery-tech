package com.deliverytech.delivery_api.repository;

import com.deliverytech.delivery_api.model.OrderItem;
import com.deliverytech.delivery_api.repository.projection.TopSellingProductProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("SELECT oi.product.name as productName, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi " +
            "WHERE oi.order.status = 'DELIVERED' " +
            "GROUP BY oi.product.name " +
            "ORDER BY totalSold DESC LIMIT 10")
    List<TopSellingProductProjection> getTopSellingProductsReport();
}
