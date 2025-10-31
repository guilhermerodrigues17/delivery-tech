package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.response.*;
import com.deliverytech.delivery_api.dto.response.errors.ErrorResponse;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import com.deliverytech.delivery_api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Relatórios", description = "Endpoints para gerenciamento de relatórios")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Listar valor total em vendas por restaurante", description = "Retorna uma lista com o valor total de vendas por restaurante")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Relatório retornado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                schema = @Schema(implementation = SalesByRestaurantReportDto.class)
                            )
                    )
            )
    })
    @GetMapping("/sales-by-restaurant")
    public ResponseEntity<List<SalesByRestaurantReportDto>> getSalesByRestaurant() {
        List<SalesByRestaurantReportDto> sales = reportService.getSalesByRestaurant();
        return ResponseEntity.ok(sales);
    }

    @Operation(summary = "Listar produtos mais vendidos por restaurante",
            description = "Retorna uma lista com os produtos mais vendidos de um restaurante")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Relatório retornado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = TopSellingProductReportDto.class)
                            )
                    )
            )
    })
    @GetMapping("/top-selling-products")
    public ResponseEntity<List<TopSellingProductReportDto>> getTopSellingProducts() {
        List<TopSellingProductReportDto> topSellingProducts = reportService.getTopSellingProducts();
        return ResponseEntity.ok(topSellingProducts);
    }

    @Operation(summary = "Listar clientes com mais pedidos", description = "Retorna uma lista de clientes baseado no número de pedidos feitos")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Relatório retornado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ActiveConsumerReportDto.class)
                            )
                    )
            )
    })
    @GetMapping("/active-consumers")
    public ResponseEntity<List<ActiveConsumerReportDto>> getActiveConsumers() {
        List<ActiveConsumerReportDto> activeConsumers = reportService.getActiveConsumers();
        return ResponseEntity.ok(activeConsumers);
    }

    @Operation(summary = "Listar pedidos por período e status",
            description = "Retorna uma lista de pedidos baseados no período especificado, podendo ser filtrado por status.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Relatório retornado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = SalesByRestaurantReportDto.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            )
    })
    @GetMapping("/orders-by-period")
    public ResponseEntity<List<OrderByPeriodReportDto>> getOrdersByPeriod(
            @Parameter(description = "Data de início do filtro", required = true, example = "2025-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Data final do filtro", required = true, example = "2025-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Status do pedido", required = false, example = "DELIVERED")
            @RequestParam(required = false) OrderStatus status
            ) {
        List<OrderByPeriodReportDto> orders = reportService.getOrdersByPeriodAndStatus(startDate, endDate, status);
        return ResponseEntity.ok(orders);
    }

}
