package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.response.ActiveConsumerReportDto;
import com.deliverytech.delivery_api.dto.response.OrderByPeriodReportDto;
import com.deliverytech.delivery_api.dto.response.SalesByRestaurantReportDto;
import com.deliverytech.delivery_api.dto.response.TopSellingProductReportDto;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import com.deliverytech.delivery_api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales-by-restaurant")
    public ResponseEntity<List<SalesByRestaurantReportDto>> getSalesByRestaurant() {
        List<SalesByRestaurantReportDto> sales = reportService.getSalesByRestaurant();
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/top-selling-products")
    public ResponseEntity<List<TopSellingProductReportDto>> getTopSellingProducts() {
        List<TopSellingProductReportDto> topSellingProducts = reportService.getTopSellingProducts();
        return ResponseEntity.ok(topSellingProducts);
    }

    @GetMapping("/active-consumers")
    public ResponseEntity<List<ActiveConsumerReportDto>> getActiveConsumers() {
        List<ActiveConsumerReportDto> activeConsumers = reportService.getActiveConsumers();
        return ResponseEntity.ok(activeConsumers);
    }

    @GetMapping("/orders-by-period")
    public ResponseEntity<List<OrderByPeriodReportDto>> getOrdersByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) OrderStatus status
            ) {
        List<OrderByPeriodReportDto> orders = reportService.getOrdersByPeriodAndStatus(startDate, endDate, status);
        return ResponseEntity.ok(orders);
    }

}
