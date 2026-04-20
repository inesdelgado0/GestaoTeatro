package com.teatro.dto;

public record UtilizadorRequestDto(
        String nome,
        String email,
        String password,
        String telemovel,
        String morada,
        String nif
) {
}
