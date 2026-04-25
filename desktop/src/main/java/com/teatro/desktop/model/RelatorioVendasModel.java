package com.teatro.desktop.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RelatorioVendasModel(
        Instant inicio,
        Instant fim,
        long totalBilhetes,
        BigDecimal totalFaturado,
        List<RelatorioVendasItemModel> itens
) {
}
