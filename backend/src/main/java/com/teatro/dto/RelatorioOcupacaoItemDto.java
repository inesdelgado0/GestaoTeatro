package com.teatro.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RelatorioOcupacaoItemDto(
        Integer sessaoId,
        String eventoTitulo,
        String salaNome,
        Instant dataHoraSessao,
        int capacidadeSala,
        long lugaresOcupados,
        BigDecimal taxaOcupacao
) {
}
