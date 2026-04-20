package com.teatro.desktop.model;

import java.math.BigDecimal;
import java.time.Instant;

public record SessaoModel(
        Integer id,
        Instant dataHora,
        BigDecimal precoBase,
        String estado,
        Integer eventoId,
        Integer salaId
) {
}
