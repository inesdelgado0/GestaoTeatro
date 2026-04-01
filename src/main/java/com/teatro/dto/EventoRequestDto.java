package com.teatro.dto;

public record EventoRequestDto(
        String titulo,
        String descricao,
        Integer duracaoMin,
        String classificacaoEtaria,
        String genero
) {
}
