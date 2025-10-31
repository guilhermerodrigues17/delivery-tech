package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.request.LoginRequestDto;
import com.deliverytech.delivery_api.dto.request.RegisterUserRequestDto;
import com.deliverytech.delivery_api.dto.response.LoginResponseDto;
import com.deliverytech.delivery_api.dto.response.RegisterResponseDto;
import com.deliverytech.delivery_api.dto.response.errors.ErrorResponse;
import com.deliverytech.delivery_api.dto.response.wrappers.ApiResponseWrapper;
import com.deliverytech.delivery_api.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para gerenciamento da autenticação de usuários no sistema.")
public class AuthController {

    private final UserServiceImpl userService;

    @Operation(summary = "Fazer login", description = "Realiza o login de um usuário que já tenha sido registrado anteriormente.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token JWT retornado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação nos dados enviados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação inválida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequestDto.class)
                    )
            )
            @Valid @RequestBody LoginRequestDto dto
    ) {
        LoginResponseDto response = userService.login(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cadastrar-se", description = "Realiza o registro de um novo usuário no sistema.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Registro feito com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Erro de validação nos dados enviados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseWrapper<RegisterResponseDto>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterUserRequestDto.class)
                    )
            )
            @Valid @RequestBody RegisterUserRequestDto dto
    ) {
        var userCreated = userService.createUser(dto);
        var response = ApiResponseWrapper.of(userCreated);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
