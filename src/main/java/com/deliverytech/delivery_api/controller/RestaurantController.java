package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.RestaurantRequestDto;
import com.deliverytech.delivery_api.dto.request.RestaurantStatusUpdateDto;
import com.deliverytech.delivery_api.dto.response.OrderSummaryResponseDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.dto.response.RestaurantResponseDto;
import com.deliverytech.delivery_api.dto.response.errors.ErrorResponse;
import com.deliverytech.delivery_api.dto.response.wrappers.ApiResponseWrapper;
import com.deliverytech.delivery_api.dto.response.wrappers.PagedResponseWrapper;
import com.deliverytech.delivery_api.mapper.RestaurantMapper;
import com.deliverytech.delivery_api.service.OrderService;
import com.deliverytech.delivery_api.service.ProductService;
import com.deliverytech.delivery_api.service.RestaurantService;
import com.deliverytech.delivery_api.validation.annotations.ValidCEP;
import com.deliverytech.delivery_api.validation.annotations.ValidCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Validated
@Tag(name = "Restaurantes", description = "Endpoints para gerenciamento de restaurantes")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final ProductService productService;
    private final OrderService orderService;
    private final RestaurantMapper mapper;

    @Operation(summary = "Cadastrar um restaurante", description = "Cadastra um novo restaurante no sistema. O nome deve ser único.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Restaurante cadastrado com sucesso"
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
                    responseCode = "409",
                    description = "Nome já cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseWrapper<RestaurantResponseDto>> createRestaurant(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantRequestDto.class)
                    )
            )
            @Valid @RequestBody RestaurantRequestDto dto
    ) {
        RestaurantResponseDto createdRestaurant = restaurantService.createRestaurant(dto);
        var response = ApiResponseWrapper.of(createdRestaurant, "Restaurante cadastrado com sucesso");
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdRestaurant.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Buscar um restaurante por ID", description = "Retorna os dados de um restaurante baseado no UUID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Restaurante encontrado com sucesso"
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
    public ResponseEntity<ApiResponseWrapper<RestaurantResponseDto>> findById(
            @Parameter(description = "ID do restaurante", required = true)
            @PathVariable String id
    ) {
        RestaurantResponseDto restaurantFound = restaurantService.findByIdResponse(id);
        var response = ApiResponseWrapper.of(restaurantFound);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar restaurantes", description = "Retorna uma lista de restaurantes, podendo filtrar nome, categoria e se está ativo")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Restaurantes listados com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
    })
    @GetMapping
    public ResponseEntity<PagedResponseWrapper<RestaurantResponseDto>> searchRestaurants(
            @Parameter(description = "Nome do restaurante", required = false)
            @RequestParam(required = false) String name,

            @Parameter(description = "Categoria do restaurante", required = false)
            @RequestParam(required = false) @ValidCategory String category,

            @Parameter(description = "Restaurante está ativo?", required = false)
            @RequestParam(required = false, defaultValue = "true") Boolean active,

            @ParameterObject Pageable pageable
    ) {
        var restaurantsPage = restaurantService.searchRestaurants(name, category, active, pageable);
        var restaurantsResponse = PagedResponseWrapper.of(restaurantsPage);
        return ResponseEntity.ok(restaurantsResponse);
    }

    @Operation(summary = "Listar produtos de um restaurante", description = "Retorna uma lista de produtos relacionados àquele restaurante")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Produtos listados com sucesso"
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
                    responseCode = "409",
                    description = "Nome já cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{restaurantId}/products")
    public ResponseEntity<PagedResponseWrapper<ProductResponseDto>> findProductsByRestaurantId(
            @Parameter(description = "ID do restaurante", required = true)
            @PathVariable String restaurantId,

            @ParameterObject Pageable pageable
    ) {
        Page<ProductResponseDto> productsPage = productService.findProductsByRestaurantId(restaurantId, pageable);
        var response = PagedResponseWrapper.of(productsPage);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar pedidos de um restaurante", description = "Retorna uma lista de pedidos relacionados àquele restaurante")
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{restaurantId}/orders")
    @PreAuthorize("hasRole('ADMIN') or @restaurantServiceImpl.isOwner(#restaurantId)")
    public ResponseEntity<PagedResponseWrapper<OrderSummaryResponseDto>> findOrdersByRestaurantId(
            @Parameter(description = "ID do restaurante", required = true)
            @PathVariable String restaurantId,

            @ParameterObject Pageable pageable
    ) {
        Page<OrderSummaryResponseDto> ordersPage = orderService.findByRestaurantId(restaurantId, pageable);
        var response = PagedResponseWrapper.of(ordersPage);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Calcular a taxa de entrega de um restaurante",
            description = "Retorna a taxa de entrega para um restaurante com base no CEP fornecido")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Taxa de entrega calculada com sucesso"
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
            ),
    })
    @GetMapping(value = "/{id}/delivery-tax", params = "cep")
    public ResponseEntity<ApiResponseWrapper<Map<String, BigDecimal>>> calculateDeliveryTax(
            @Parameter(description = "ID do restaurante", required = true)
            @PathVariable String id,

            @Parameter(description = "CEP do cliente", required = true, example = "12345-678")
            @RequestParam(required = true)
            @ValidCEP String cep
    ) {
        BigDecimal deliveryTax = restaurantService.calculateDeliveryTax(id, cep);

        Map<String, BigDecimal> deliveryTaxMap = new HashMap<String, BigDecimal>();
        deliveryTaxMap.put("deliveryTax", deliveryTax);

        var response = ApiResponseWrapper.of(deliveryTaxMap);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar restaurantes próximos", description = "Retorna uma lista de restaurantes próximos baseado no CEP fornecido")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Restaurantes listados com sucesso"
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
    @GetMapping(value = "/search/nearby", params = "cep")
    public ResponseEntity<PagedResponseWrapper<RestaurantResponseDto>> findRestaurantsNearby(
            @Parameter(description = "CEP do cliente", required = true, example = "12345-678")
            @RequestParam
            @ValidCEP String cep,

            @ParameterObject Pageable pageable
    ) {
        var restaurantsPage = restaurantService.findRestaurantsNearby(cep, pageable);
        var response = PagedResponseWrapper.of(restaurantsPage);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar dados de um restaurante", description = "Atualiza os dados de um restaurante com base no seu UUID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados atualizados com sucesso"
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
                    responseCode = "409",
                    description = "Nome já cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @restaurantServiceImpl.isOwner(#id)")
    public ResponseEntity<ApiResponseWrapper<RestaurantResponseDto>> updateRestaurant(
            @Parameter(description = "ID do restaurante a ser atualizado", required = true)
            @PathVariable String id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantRequestDto.class)
                    )
            )
            @Valid @RequestBody RestaurantRequestDto dto
    ) {
        var updatedRestaurant = restaurantService.updateRestaurant(id, dto);
        var response = ApiResponseWrapper.of(updatedRestaurant, "Dados atualizados com sucesso");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar status de um restaurante", description = "Atualiza o status de um restaurante com base no seu UUID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Dados atualizados com sucesso"
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
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or @restaurantServiceImpl.isOwner(#id)")
    public ResponseEntity<Void> updateStatusActive(
            @Parameter(description = "ID do restaurante a ser atualizado", required = true)
            @PathVariable String id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantStatusUpdateDto.class)
                    )
            )
            @Valid @RequestBody RestaurantStatusUpdateDto dto
    ) {
        restaurantService.updateStatusActive(id, dto);
        return ResponseEntity.noContent().build();
    }
}
