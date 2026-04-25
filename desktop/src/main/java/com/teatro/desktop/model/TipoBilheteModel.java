package com.teatro.desktop.model;

import java.math.BigDecimal;

public record TipoBilheteModel(
        Integer id,
        String nome,
        BigDecimal percentagemDesconto
) {
}
