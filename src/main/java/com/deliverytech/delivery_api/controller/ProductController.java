package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.OrderRequestDto;
import com.deliverytech.delivery_api.dto.request.ProductRequestDto;
import com.deliverytech.delivery_api.dto.response.OrderResponseDto;
import com.deliverytech.delivery_api.dto.response.ProductResponseDto;
import com.deliverytech.delivery_api.exceptions.ErrorMessage;
import com.deliverytech.delivery_api.mapper.ProductMapper;
import com.deliverytech.delivery_api.model.Product;
import com.deliverytech.delivery_api.service.ProductService;
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
                    description = "Produto criado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductRequestDto.class)
                    )
            )
            @Valid @RequestBody ProductRequestDto dto
    ) {
        Product product = productService.createProduct(dto);
        var response = productMapper.toResponseDto(product);

        return ResponseEntity.created(null).body(response);
    }

    @Operation(summary = "Listar produtos", description = "Retorna uma lista de produtos, podendo ser filtrados por nome e categoria.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Produtos listados com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ProductResponseDto.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            )
    })
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDto>> searchProducts(
            @Parameter(description = "Nome do produto", required = false, example = "Lasanha")
            @RequestParam(required = false) String name,

            @Parameter(description = "Categoria do produto", required = false, example = "Massas")
            @RequestParam(required = false) String category
    ) {
        List<ProductResponseDto> products = productService.searchProducts(name, category);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Buscar um produto por ID", description = "Retorna um produto baseado no UUID fornecido.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto encontrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> findProductById(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable String id
    ) {
        var response = productService.findProductByIdResponse(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar dados de um produto", description = "Atualiza os dados de um produto baseado no seu UUID.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados atualizados com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação (ex: formato inválido ou dados faltando)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Entidade não processável",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            ),
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
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
        var response = productService.updateProduct(id, dto);
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
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            )
    })
    @PatchMapping("/{id}/status")
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
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID do produto", required = true)
            @PathVariable String id
    ) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
