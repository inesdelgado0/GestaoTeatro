package com.teatro.dto;

public record AuthLoginResponseDto(
        Integer id,
        String nome,
        String email,
        String tipoUtilizador
) {
}
