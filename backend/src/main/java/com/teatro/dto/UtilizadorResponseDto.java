package com.teatro.dto;

public record UtilizadorResponseDto(
        Integer id,
        String nome,
        String email,
        String telemovel,
        String morada,
        String nif,
        String tipoUtilizador
) {
}
