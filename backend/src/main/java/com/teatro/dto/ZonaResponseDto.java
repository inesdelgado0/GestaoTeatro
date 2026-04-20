package com.teatro.dto;

import java.math.BigDecimal;

public record ZonaResponseDto(
        Integer id,
        String nome,
        BigDecimal taxaAdicional,
        Integer salaId
) {
}
