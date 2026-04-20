package com.teatro.desktop.model;

import java.math.BigDecimal;

public record ZonaModel(
        Integer id,
        String nome,
        BigDecimal taxaAdicional,
        Integer salaId
) {
}
