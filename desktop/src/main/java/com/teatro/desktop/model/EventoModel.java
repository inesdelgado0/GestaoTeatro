package com.teatro.desktop.model;

public record EventoModel(
        Integer id,
        String titulo,
        String descricao,
        Integer duracaoMin,
        String classificacaoEtaria,
        String genero
) {
}
