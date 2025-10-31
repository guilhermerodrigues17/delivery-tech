package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.dto.response.errors.ErrorResponse;
import com.deliverytech.delivery_api.dto.response.wrappers.ApiResponseWrapper;
import com.deliverytech.delivery_api.dto.response.wrappers.PagedResponseWrapper;
import com.deliverytech.delivery_api.mapper.ProductMapper;
import com.deliverytech.delivery_api.service.ProductService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @Operation(summary = "Criar um novo produto", description = "Cria um novo produto para um restaurante")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Produto criado com sucesso"
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
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT') and @restaurantServiceImpl.isOwner(#dto.restaurantId))")
    public ResponseEntity<ApiResponseWrapper<ProductResponseDto>> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductRequestDto.class)
                    )
            )
            @Valid @RequestBody ProductRequestDto dto
    ) {
        ProductResponseDto productCreated = productService.createProduct(dto);
        var response = ApiResponseWrapper.of(productCreated, "Produto criado com sucesso");
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(productCreated.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Listar produtos", description = "Retorna uma lista de produtos, podendo ser filtrados por nome e categoria.")
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
            )
    })
    @GetMapping("/search")
    public ResponseEntity<PagedResponseWrapper<ProductResponseDto>> searchProducts(
            @Parameter(description = "Nome do produto", required = false, example = "Lasanha")
            @RequestParam(required = false) String name,

            @Parameter(description = "Categoria do produto", required = false, example = "Massas")
            @RequestParam(required = false) String category,

            @ParameterObject Pageable pageable
    ) {
        Page<ProductResponseDto> productsPage = productService.searchProducts(name, category, pageable);
        var productsResponse = PagedResponseWrapper.of(productsPage);
        return ResponseEntity.ok(productsResponse);
    }

    @Operation(summary = "Buscar um produto por ID", description = "Retorna um produto baseado no UUID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto encontrado com sucesso"
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
    public ResponseEntity<ApiResponseWrapper<ProductResponseDto>> findProductById(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable String id
    ) {
        ProductResponseDto productFound = productService.findProductByIdResponse(id);
        var response = ApiResponseWrapper.of(productFound);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar dados de um produto", description = "Atualiza os dados de um produto baseado no seu UUID.")
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
                    responseCode = "422",
                    description = "Entidade não processável",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT') and @productServiceImpl.isOwnerOfProductRestaurant(#id))")
    public ResponseEntity<ApiResponseWrapper<ProductResponseDto>> updateProduct(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable String id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductRequestDto.class)
                    )
            )
            @Valid @RequestBody ProductRequestDto dto
    ) {
        ProductResponseDto updatedProduct = productService.updateProduct(id, dto);
        var response = ApiResponseWrapper.of(updatedProduct, "Dados atualizados com sucesso");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Alterar disponibilidade de um produto", description = "Altera a disponibilidade de um produto baseado no seu UUID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Disponibilidade alterada com sucesso"
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
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT') and @productServiceImpl.isOwnerOfProductRestaurant(#id))")
    public ResponseEntity<Void> toggleAvailability(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable String id
    ) {
        productService.toggleAvailability(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Apagar um produto", description = "Apaga um produto baseado no seu UUID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Produto apagado com sucesso"
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
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT') and @productServiceImpl.isOwnerOfProductRestaurant(#id))")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable String id
    ) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
