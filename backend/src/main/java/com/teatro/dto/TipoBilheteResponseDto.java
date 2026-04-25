package com.teatro.dto;

import java.math.BigDecimal;

public record TipoBilheteResponseDto(
        Integer id,
        String nome,
        BigDecimal percentagemDesconto
) {
}
