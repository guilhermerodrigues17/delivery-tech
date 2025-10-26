package com.deliverytech.delivery_api.repository;

import com.deliverytech.delivery_api.model.Order;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import com.deliverytech.delivery_api.repository.projection.ActiveConsumerProjection;
import com.deliverytech.delivery_api.repository.projection.OrderByPeriodProjection;
import com.deliverytech.delivery_api.repository.projection.SalesByRestaurantProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    List<Order> findByConsumerId(UUID consumerId);

    Page<Order> findByRestaurantId(UUID restaurantId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findTop10ByOrderByOrderDateDesc();

    @Query("SELECT o.restaurant.name as restaurantName, SUM(o.total) as totalSales FROM Order o "
            + "WHERE o.status = 'DELIVERED' "
            + "GROUP BY o.restaurant.id, o.restaurant.name "
            + "ORDER BY totalSales DESC")
    List<SalesByRestaurantProjection> getSalesByRestaurantReport();

    @Query("SELECT o FROM Order o WHERE o.total > :value ORDER BY o.total DESC")
    List<Order> findOrdersWithTotalGreaterThan(@Param("value") BigDecimal value);

    @Query("SELECT CAST(o.orderDate AS DATE) as date, COUNT(o.id) as totalOrders, " +
            "SUM(o.total) as totalSales, o.status as status " +
            "FROM Order o " +
            "WHERE (CAST(:status as string) IS NULL OR o.status = :status) " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(o.orderDate AS DATE), o.status " +
            "ORDER BY date ASC")
    List<OrderByPeriodProjection> getOrdersByPeriod(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    @Param("status") OrderStatus status);

    @Query("SELECT o.consumer.name as consumerName, o.consumer.email as consumerEmail, COUNT (o.id) as totalOrders " +
            "FROM Order o WHERE o.status = 'DELIVERED' " +
            "GROUP BY o.consumer.id, o.consumer.name, o.consumer.email " +
            "ORDER BY totalOrders DESC LIMIT 5")
    List<ActiveConsumerProjection> getActiveConsumers();
}
