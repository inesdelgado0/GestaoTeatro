package com.teatro.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RelatorioVendasItemDto(
        Integer sessaoId,
        String eventoTitulo,
        String salaNome,
        Instant dataHoraSessao,
        long totalBilhetes,
        BigDecimal totalFaturado
) {
}
