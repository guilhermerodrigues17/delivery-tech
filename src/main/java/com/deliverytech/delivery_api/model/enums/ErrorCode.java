package com.deliverytech.delivery_api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR("VALIDATION_ERROR", "Erro de validação nos dados enviados."),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "O recurso solicitado não foi encontrado."),
    CONFLICT_ERROR("CONFLICT_ERROR", "Conflito de dados (ex: registro duplicado)."),
    FORBIDDEN_ACCESS("FORBIDDEN_ACCESS", "Acesso ou operação não permitida."),
    UNAUTHORIZED_ERROR("UNAUTHORIZED_ERROR", "Autenticação inválida"),
    UNPROCESSABLE_ENTITY("UNPROCESSABLE_ENTITY", "A requisição está semanticamente incorreta (ex: regra de negócio violada)."),
    BAD_REQUEST("BAD_REQUEST", "Requisição mal formada ou inválida."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Erro inesperado no servidor.");

    private final String code;
    private final String defaultMessage;
}
