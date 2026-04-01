package com.teatro.dto;

import com.teatro.entities.EstadoSessao;

import java.math.BigDecimal;
import java.time.Instant;

public record SessaoRequestDto(
        Instant dataHora,
        BigDecimal precoBase,
        EstadoSessao estado,
        Integer eventoId,
        Integer salaId
) {
}
