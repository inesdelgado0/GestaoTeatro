package com.teatro.desktop.model;

import java.math.BigDecimal;
import java.time.Instant;

public record RelatorioVendasItemModel(
        Integer sessaoId,
        String eventoTitulo,
        String salaNome,
        Instant dataHoraSessao,
        long totalBilhetes,
        BigDecimal totalFaturado
) {
}
