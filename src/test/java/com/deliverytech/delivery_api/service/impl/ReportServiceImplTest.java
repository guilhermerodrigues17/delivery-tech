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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportServiceImpl reportServiceImpl;

    @Captor
    private ArgumentCaptor<LocalDateTime> startDateTimeCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> endDateTimeCaptor;

    @Nested
    @DisplayName("getOrdersByPeriodAndStatus() tests")
    class GetOrdersByPeriodTests {

        private LocalDate startDate;
        private LocalDate endDate;

        @BeforeEach
        void setUp() {
            startDate = LocalDate.of(2025, 10, 1);
            endDate = LocalDate.of(2025, 10, 31);
        }

        @Test
        @DisplayName("Should throw BusinessException when startDate is null")
        void should_ThrowBusinessException_When_StartDateIsNull() {
            startDate = null;

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                reportServiceImpl.getOrdersByPeriodAndStatus(startDate, endDate, OrderStatus.DELIVERED);
            });

            assertEquals("Data de início e data de fim são obrigatórias para este relatório.", exception.getMessage());

            verify(orderRepository, never()).getOrdersByPeriod(any(), any(), any());
            verify(reportMapper, never()).toOrderByPeriodAndStatusDtoList(any());
        }

        @Test
        @DisplayName("Should throw BusinessException when endDate is null")
        void should_ThrowBusinessException_When_EndDateIsNull() {
            endDate = null;

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                reportServiceImpl.getOrdersByPeriodAndStatus(startDate, endDate, OrderStatus.DELIVERED);
            });

            assertEquals("Data de início e data de fim são obrigatórias para este relatório.", exception.getMessage());

            verify(orderRepository, never()).getOrdersByPeriod(any(), any(), any());
            verify(reportMapper, never()).toOrderByPeriodAndStatusDtoList(any());
        }

        @Test
        @DisplayName("Should return report DTO list and call repository with correct LocalTime")
        void should_ReturnDtoList_When_DatesAreValid() {
            LocalDateTime expectedStartDateTime = startDate.atStartOfDay();
            LocalDateTime expectedEndDateTime = endDate.atTime(LocalTime.MAX);

            List<OrderByPeriodProjection> mockProjections = Collections.emptyList();
            List<OrderByPeriodReportDto> expectedDtos = Collections.emptyList();

            when(orderRepository.getOrdersByPeriod(
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    any(OrderStatus.class)
            )).thenReturn(mockProjections);
            when(reportMapper.toOrderByPeriodAndStatusDtoList(mockProjections)).thenReturn(expectedDtos);

            List<OrderByPeriodReportDto> actualDtos = reportServiceImpl.getOrdersByPeriodAndStatus(startDate, endDate, OrderStatus.DELIVERED);

            assertNotNull(actualDtos);
            assertEquals(expectedDtos, actualDtos);

            verify(orderRepository).getOrdersByPeriod(
                    startDateTimeCaptor.capture(),
                    endDateTimeCaptor.capture(),
                    eq(OrderStatus.DELIVERED)
            );

            assertEquals(expectedStartDateTime, startDateTimeCaptor.getValue());
            assertEquals(expectedEndDateTime, endDateTimeCaptor.getValue());

            verify(reportMapper).toOrderByPeriodAndStatusDtoList(mockProjections);
        }
    }

    @Nested
    @DisplayName("getSalesByRestaurant() tests")
    class GetSalesByRestaurantTests {

        @Test
        @DisplayName("Should return DTO list when repository finds projections")
        void should_ReturnDtoList_When_ProjectionsAreFound() {
            SalesByRestaurantProjection mockProjection = mock(SalesByRestaurantProjection.class);
            List<SalesByRestaurantProjection> mockProjectionsList = List.of(mockProjection);

            SalesByRestaurantReportDto responseDto = new SalesByRestaurantReportDto("Test Restaurant", new BigDecimal("1000.00"));
            List<SalesByRestaurantReportDto> expectedDtoList = List.of(responseDto);

            when(orderRepository.getSalesByRestaurantReport()).thenReturn(mockProjectionsList);
            when(reportMapper.toDtoList(mockProjectionsList)).thenReturn(expectedDtoList);

            List<SalesByRestaurantReportDto> actualDtoList = reportServiceImpl.getSalesByRestaurant();

            assertNotNull(actualDtoList);
            assertEquals(1, actualDtoList.size());
            assertEquals(expectedDtoList, actualDtoList);
            assertEquals("Test Restaurant", actualDtoList.getFirst().restaurantName());

            verify(orderRepository).getSalesByRestaurantReport();
            verify(reportMapper).toDtoList(mockProjectionsList);
        }

        @Test
        @DisplayName("Should return empty list when repository finds no projections")
        void should_ReturnEmptyList_When_NoProjectionsAreFound() {
            List<SalesByRestaurantProjection> mockProjectionsList = Collections.emptyList();
            List<SalesByRestaurantReportDto> expectedDtoList = Collections.emptyList();

            when(orderRepository.getSalesByRestaurantReport()).thenReturn(mockProjectionsList);
            when(reportMapper.toDtoList(mockProjectionsList)).thenReturn(expectedDtoList);

            List<SalesByRestaurantReportDto> actualDtoList = reportServiceImpl.getSalesByRestaurant();

            assertNotNull(actualDtoList);
            assertTrue(actualDtoList.isEmpty());

            verify(orderRepository).getSalesByRestaurantReport();
            verify(reportMapper).toDtoList(mockProjectionsList);
        }
    }

    @Nested
    @DisplayName("getTopSellingProducts() tests")
    class GetTopSellingProductsTests {

        @Test
        @DisplayName("Should return DTO list when repository finds projections")
        void should_ReturnDtoList_When_ProjectionsAreFound() {
            TopSellingProductProjection mockProjection = mock(TopSellingProductProjection.class);
            List<TopSellingProductProjection> mockProjectionsList = List.of(mockProjection);

            TopSellingProductReportDto responseDto = new TopSellingProductReportDto("Pizza", 50L);
            List<TopSellingProductReportDto> expectedDtoList = List.of(responseDto);

            when(orderItemRepository.getTopSellingProductsReport()).thenReturn(mockProjectionsList);
            when(reportMapper.toTopSellingDtoList(mockProjectionsList)).thenReturn(expectedDtoList);

            List<TopSellingProductReportDto> actualDtoList = reportServiceImpl.getTopSellingProducts();

            assertNotNull(actualDtoList);
            assertEquals(1, actualDtoList.size());
            assertEquals(expectedDtoList, actualDtoList);
            assertEquals(50L, actualDtoList.getFirst().totalSold());

            verify(orderItemRepository).getTopSellingProductsReport();
            verify(reportMapper).toTopSellingDtoList(mockProjectionsList);
        }

        @Test
        @DisplayName("Should return empty list when repository finds no projections")
        void should_ReturnEmptyList_When_NoProjectionsAreFound() {
            List<TopSellingProductProjection> mockProjectionsList = Collections.emptyList();
            List<TopSellingProductReportDto> expectedDtoList = Collections.emptyList();

            when(orderItemRepository.getTopSellingProductsReport()).thenReturn(mockProjectionsList);
            when(reportMapper.toTopSellingDtoList(mockProjectionsList)).thenReturn(expectedDtoList);

            List<TopSellingProductReportDto> actualDtoList = reportServiceImpl.getTopSellingProducts();

            assertNotNull(actualDtoList);
            assertTrue(actualDtoList.isEmpty());

            verify(orderItemRepository).getTopSellingProductsReport();
            verify(reportMapper).toTopSellingDtoList(mockProjectionsList);
        }
    }

    @Nested
    @DisplayName("getActiveConsumers() tests")
    class GetActiveConsumersTests {

        @Test
        @DisplayName("Should return DTO list when repository finds projections")
        void should_ReturnDtoList_When_ProjectionsAreFound() {
            ActiveConsumerProjection mockProjection = mock(ActiveConsumerProjection.class);
            List<ActiveConsumerProjection> mockProjectionsList = List.of(mockProjection);

            ActiveConsumerReportDto responseDto = new ActiveConsumerReportDto(
                    "Test Consumer", "consumer@email.com", 20L
            );
            List<ActiveConsumerReportDto> expectedDtoList = List.of(responseDto);

            when(orderRepository.getActiveConsumers()).thenReturn(mockProjectionsList);
            when(reportMapper.toActiveConsumerDtoList(mockProjectionsList)).thenReturn(expectedDtoList);

            List<ActiveConsumerReportDto> actualDtoList = reportServiceImpl.getActiveConsumers();

            assertNotNull(actualDtoList);
            assertEquals(1, actualDtoList.size());
            assertEquals(expectedDtoList, actualDtoList);
            assertEquals(20L, actualDtoList.getFirst().totalOrders());

            verify(orderRepository).getActiveConsumers();
            verify(reportMapper).toActiveConsumerDtoList(mockProjectionsList);
        }

        @Test
        @DisplayName("Should return empty list when repository finds no projections")
        void should_ReturnEmptyList_When_NoProjectionsAreFound() {
            List<ActiveConsumerProjection> mockProjectionsList = Collections.emptyList();
            List<ActiveConsumerReportDto> expectedDtoList = Collections.emptyList();

            when(orderRepository.getActiveConsumers()).thenReturn(mockProjectionsList);
            when(reportMapper.toActiveConsumerDtoList(mockProjectionsList)).thenReturn(expectedDtoList);

            List<ActiveConsumerReportDto> actualDtoList = reportServiceImpl.getActiveConsumers();


            assertNotNull(actualDtoList);
            assertTrue(actualDtoList.isEmpty());

            verify(orderRepository).getActiveConsumers();
            verify(reportMapper).toActiveConsumerDtoList(mockProjectionsList);
        }
    }
}