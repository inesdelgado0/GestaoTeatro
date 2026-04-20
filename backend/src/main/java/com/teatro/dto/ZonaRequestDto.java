package com.teatro.dto;

import java.math.BigDecimal;

public record ZonaRequestDto(
        String nome,
        BigDecimal taxaAdicional,
        Integer salaId
) {
}
