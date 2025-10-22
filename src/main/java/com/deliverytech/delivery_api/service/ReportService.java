package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.response.SalesByRestaurantReportDto;

import java.util.List;

public interface ReportService {
    List<SalesByRestaurantReportDto> getSalesByRestaurant();
}
