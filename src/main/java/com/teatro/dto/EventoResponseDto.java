package com.teatro.dto;

public record EventoResponseDto(
        Integer id,
        String titulo,
        String descricao,
        Integer duracaoMin,
        String classificacaoEtaria,
        String genero
) {
}
