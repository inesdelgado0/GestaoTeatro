package com.teatro.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RelatorioVendasDto(
        Instant inicio,
        Instant fim,
        long totalBilhetes,
        BigDecimal totalFaturado,
        List<RelatorioVendasItemDto> itens
) {
}
