package com.teatro.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SessaoResponseDto(
        Integer id,
        Instant dataHora,
        BigDecimal precoBase,
        String estado,
        Integer eventoId,
        Integer salaId
) {
}
