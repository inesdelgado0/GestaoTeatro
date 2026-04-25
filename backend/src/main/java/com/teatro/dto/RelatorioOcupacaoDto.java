package com.teatro.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RelatorioOcupacaoDto(
        Instant inicio,
        Instant fim,
        BigDecimal taxaMediaOcupacao,
        List<RelatorioOcupacaoItemDto> itens
) {
}
