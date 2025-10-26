package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.OrderRequestDto;
import com.deliverytech.delivery_api.dto.request.OrderStatusUpdateRequestDto;
import com.deliverytech.delivery_api.dto.response.OrderResponseDto;
import com.deliverytech.delivery_api.dto.response.OrderSummaryResponseDto;
import com.deliverytech.delivery_api.dto.response.OrderTotalResponseDto;
import com.deliverytech.delivery_api.dto.response.errors.ErrorResponse;
import com.deliverytech.delivery_api.dto.response.wrappers.ApiResponseWrapper;
import com.deliverytech.delivery_api.dto.response.wrappers.PagedResponseWrapper;
import com.deliverytech.delivery_api.model.enums.OrderStatus;
import com.deliverytech.delivery_api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Endpoints para gerenciamento de pedidos")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Criar um novo pedido", description = "Cria um novo pedido no sistema.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido criado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato de UUID inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Entidade não processável",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponseWrapper<OrderResponseDto>> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados necessários para montar um pedido",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderRequestDto.class)
                    )
            )
            @Valid @RequestBody OrderRequestDto dto
    ) {
        OrderResponseDto orderCreated = orderService.createOrder(dto);
        var response = ApiResponseWrapper.of(orderCreated, "Pedido criado com sucesso");
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(orderCreated.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Calcular valor total", description = "Calcula o valor total do pedido sem persistir os dados")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Calculo realizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Entidade não processável",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponseWrapper<OrderTotalResponseDto>> calculateOrderTotal(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados necessários para calcular o valor total do pedido",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderRequestDto.class)
                    )
            )
            @Valid @RequestBody OrderRequestDto dto
    ) {
        OrderTotalResponseDto calculatedOrderTotal = orderService.calculateOrderTotal(dto);
        var response = ApiResponseWrapper.of(calculatedOrderTotal);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar pedidos", description = "Retorna uma lista de pedidos, podendo filtrar por data e status")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedidos listados com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<PagedResponseWrapper<OrderSummaryResponseDto>> searchOrders(
            @Parameter(description = "Filtrar pedidos pelo status", example = "DELIVERED", required = false)
            @RequestParam(required = false) OrderStatus status,

            @Parameter(description = "Data inicial do período de busca", example = "2025-10-01", required = false)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Data final do período de busca", example = "2025-10-02", required = false)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @ParameterObject Pageable pageable
            ) {
        Page<OrderSummaryResponseDto> ordersPage = orderService.searchOrders(status, startDate, endDate, pageable);
        var ordersResponse = PagedResponseWrapper.of(ordersPage);
        return ResponseEntity.ok(ordersResponse);
    }

    @Operation(summary = "Buscar pedido por ID", description = "Retorna os dados de um pedido baseado no UUID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedidos encontrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<OrderResponseDto>> findOrderById(
            @Parameter(description = "ID de busca do pedido", required = true)
            @PathVariable String id
    ) {
        OrderResponseDto orderFound = orderService.getOrderResponseById(id);
        var response = ApiResponseWrapper.of(orderFound);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar status de um pedido", description = "Atualiza o status do pedido relacionado ao UUID fornecido")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Status atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Entidade não processável",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<OrderResponseDto>> updateOrderStatus(
            @Parameter(description = "ID do pedido a ser atualizado", required = true)
            @PathVariable String id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados necessários para atualizar o status do pedido",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderStatusUpdateRequestDto.class)
                    )
            )
            @RequestBody OrderStatusUpdateRequestDto dto
    ) {
        OrderResponseDto updatedOrder = orderService.updateOrderStatus(id, dto.getStatus());
        var response = ApiResponseWrapper.of(updatedOrder, "O status do pedido foi atualizado");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancelar um pedido", description = "Cancela um pedido com base no UUID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Pedido cancelado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "ID do pedido a ser cancelado", required = true)
            @PathVariable("id") String orderId
    ) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
