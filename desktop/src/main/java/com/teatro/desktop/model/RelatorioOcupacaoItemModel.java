package com.teatro.desktop.model;

import java.math.BigDecimal;
import java.time.Instant;

public record RelatorioOcupacaoItemModel(
        Integer sessaoId,
        String eventoTitulo,
        String salaNome,
        Instant dataHoraSessao,
        int capacidadeSala,
        long lugaresOcupados,
        BigDecimal taxaOcupacao
) {
}
