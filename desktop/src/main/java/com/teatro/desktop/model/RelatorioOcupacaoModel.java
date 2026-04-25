package com.teatro.desktop.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RelatorioOcupacaoModel(
        Instant inicio,
        Instant fim,
        BigDecimal taxaMediaOcupacao,
        List<RelatorioOcupacaoItemModel> itens
) {
}
