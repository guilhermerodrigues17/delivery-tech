package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.response.SalesByRestaurantReportDto;
import com.deliverytech.delivery_api.dto.response.TopSellingProductReportDto;

import java.util.List;

public interface ReportService {
    List<SalesByRestaurantReportDto> getSalesByRestaurant();
    List<TopSellingProductReportDto> getTopSellingProducts();
}
