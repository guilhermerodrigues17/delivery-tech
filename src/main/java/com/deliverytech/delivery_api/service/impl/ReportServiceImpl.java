package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.response.ActiveConsumerReportDto;
import com.deliverytech.delivery_api.dto.response.OrderByPeriodReportDto;
import com.deliverytech.delivery_api.dto.response.SalesByRestaurantReportDto;
import com.deliverytech.delivery_api.dto.response.TopSellingProductReportDto;
import com.deliverytech.delivery_api.exceptions.BusinessException;
import com.deliverytech.delivery_api.mapper.ReportMapper;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import com.deliverytech.delivery_api.repository.OrderItemRepository;
import com.deliverytech.delivery_api.repository.OrderRepository;
import com.deliverytech.delivery_api.repository.projection.ActiveConsumerProjection;
import com.deliverytech.delivery_api.repository.projection.OrderByPeriodProjection;
import com.deliverytech.delivery_api.repository.projection.SalesByRestaurantProjection;
import com.deliverytech.delivery_api.repository.projection.TopSellingProductProjection;
import com.deliverytech.delivery_api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReportMapper reportMapper;

    @Transactional(readOnly = true)
    public List<SalesByRestaurantReportDto> getSalesByRestaurant() {
        List<SalesByRestaurantProjection> projections = orderRepository.getSalesByRestaurantReport();
        return reportMapper.toDtoList(projections);
    }

    @Transactional(readOnly = true)
    public List<TopSellingProductReportDto> getTopSellingProducts() {
        List<TopSellingProductProjection> projections = orderItemRepository.getTopSellingProductsReport();
        return reportMapper.toTopSellingDtoList(projections);
    }

    @Transactional(readOnly = true)
    public List<ActiveConsumerReportDto> getActiveConsumers() {
        List<ActiveConsumerProjection> projections = orderRepository.getActiveConsumers();
        return reportMapper.toActiveConsumerDtoList(projections);
    }

    @Transactional(readOnly = true)
    public List<OrderByPeriodReportDto> getOrdersByPeriodAndStatus(LocalDate startDate, LocalDate endDate,
                                                                   OrderStatus status) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("Data de início e data de fim são obrigatórias para este relatório.");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<OrderByPeriodProjection> projections = orderRepository
                .getOrdersByPeriod(startDateTime, endDateTime, status);
        return reportMapper.toOrderByPeriodAndStatusDtoList(projections);
    }
}
