package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.ConsumerRequestDto;
import com.deliverytech.delivery_api.dto.response.ConsumerResponseDto;
import com.deliverytech.delivery_api.dto.response.OrderSummaryResponseDto;
import com.deliverytech.delivery_api.exceptions.ErrorMessage;
import com.deliverytech.delivery_api.mapper.ConsumerMapper;
import com.deliverytech.delivery_api.model.Consumer;
import com.deliverytech.delivery_api.service.ConsumerService;
import com.deliverytech.delivery_api.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/consumers")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes")
public class ConsumerController {

    private final ConsumerService consumerService;
    private final OrderService orderService;
    private final ConsumerMapper mapper;

    @Operation(summary = "Cria um novo cliente", description = "Cadastra um novo cliente no sistema. O E-mail deve ser único")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente criado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConsumerResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato de e-mail inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail já cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ConsumerResponseDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConsumerRequestDto.class)
                    )
            )
            @Valid @RequestBody ConsumerRequestDto dto
    ) {
        Consumer consumer = mapper.toEntity(dto);
        Consumer consumerCreated = consumerService.create(consumer);
        var response = mapper.toDto(consumerCreated);
        return ResponseEntity.created(null).body(response);
    }

    @Operation(summary = "Busca um cliente por ID", description = "Retorna os dados de um cliente específico baseado no seu UUID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConsumerResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsumerResponseDto> findById(
            @Parameter(description = "UUID do cliente a ser buscado", required = true)
            @PathVariable String id
    ) {
        UUID uuid = UUID.fromString(id);
        Consumer consumer = consumerService.findById(uuid);
        var response = mapper.toDto(consumer);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lista todos os clientes ativos", description = "Retorna uma lista com os dados dos clientes ativos encontrados")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de clientes encontrados",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ConsumerResponseDto.class)
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<ConsumerResponseDto>> findAllActive() {
        List<Consumer> consumers = consumerService.findAllActive();
        var response = consumers.stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Busca um cliente por e-mail", description = "Retorna os dados de um cliente específico baseado no e-mail")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConsumerResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            ),
    })
    @GetMapping(params = "email")
    public ResponseEntity<ConsumerResponseDto> findConsumerByEmail(
            @Parameter(description = "E-mail do cliente a ser buscado", required = true)
            @RequestParam String email) {
        var consumer = consumerService.findByEmail(email);
        var response = mapper.toDto(consumer);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lista pedidos de um cliente pelo ID",
            description = "Retorna uma lista com os dados dos pedidos finalizados do cliente, a partir do seu UUID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pedidos encontrados",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = OrderSummaryResponseDto.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            )
    })
    @GetMapping("/{consumerId}/orders")
    public ResponseEntity<List<OrderSummaryResponseDto>> findOrdersByConsumerId(
            @Parameter(description = "ID do cliente", required = true)
            @PathVariable String consumerId
    ) {
        var ordersResponse = orderService.findByConsumerIdResponse(consumerId);
        return ResponseEntity.ok(ordersResponse);
    }

    @Operation(summary = "Atualiza dados de um cliente por ID", description = "Retorna os dados atualizados do cliente")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados atualizados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConsumerResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato de e-mail inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail já cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ConsumerResponseDto> updateConsumer(
            @Parameter(description = "ID do cliente a ser atualizado", required = true)
            @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConsumerRequestDto.class)
                    )
            )
            @Valid @RequestBody ConsumerRequestDto dto
    ) {
        var response = consumerService.updateConsumer(id, dto);
        return ResponseEntity.ok(response);

    }

    @Operation(summary = "Desativa um cliente por ID", description = "Desativa um cliente com base no UUID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Cliente desativado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class))
            ),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteConsumer(
            @Parameter(description = "ID do ciente a ser desativado", required = true)
            @PathVariable String id
    ) {
        consumerService.softDeleteConsumer(id);
        return ResponseEntity.noContent().build();
    }
}
