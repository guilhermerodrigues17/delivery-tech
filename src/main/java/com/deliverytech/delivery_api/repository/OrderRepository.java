package com.deliverytech.delivery_api.repository;

import com.deliverytech.delivery_api.model.Order;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
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

    List<Order> findByRestaurantId(UUID restaurantId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findTop10ByOrderByOrderDateDesc();

    @Query("SELECT o.restaurant.name, SUM(o.total) FROM Order o "
            + "GROUP BY o.restaurant.id, o.restaurant.name "
            + "ORDER BY SUM(o.total) DESC")
    List<Object[]> calculateTotalSalesPerRestaurant();

    @Query("SELECT o FROM Order o WHERE o.total > :value ORDER BY o.total DESC")
    List<Order> findOrdersWithTotalGreaterThan(@Param("value") BigDecimal value);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.status = :status "
            + "ORDER BY o.orderDate DESC")
    List<Order> reportOrdersByPeriodAndStatus(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              @Param("status") OrderStatus status);
}
