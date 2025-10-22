package com.deliverytech.delivery_api.mapper;

import com.deliverytech.delivery_api.dto.response.SalesByRestaurantReportDto;
import com.deliverytech.delivery_api.dto.response.TopSellingProductReportDto;
import com.deliverytech.delivery_api.repository.projection.SalesByRestaurantProjection;
import com.deliverytech.delivery_api.repository.projection.TopSellingProductProjection;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    List<SalesByRestaurantReportDto> toDtoList(List<SalesByRestaurantProjection> projections);

    List<TopSellingProductReportDto> toTopSellingDtoList(List<TopSellingProductProjection> projections);
}
