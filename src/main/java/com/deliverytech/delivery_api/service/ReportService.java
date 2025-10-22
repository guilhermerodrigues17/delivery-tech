package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.response.ActiveConsumerReportDto;
import com.deliverytech.delivery_api.dto.response.OrderByPeriodReportDto;
import com.deliverytech.delivery_api.dto.response.SalesByRestaurantReportDto;
import com.deliverytech.delivery_api.dto.response.TopSellingProductReportDto;
import com.deliverytech.delivery_api.model.enums.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    List<SalesByRestaurantReportDto> getSalesByRestaurant();
    List<TopSellingProductReportDto> getTopSellingProducts();
    List<ActiveConsumerReportDto> getActiveConsumers();
    List<OrderByPeriodReportDto> getOrdersByPeriodAndStatus(LocalDate startDate, LocalDate endDate, OrderStatus status);
}
