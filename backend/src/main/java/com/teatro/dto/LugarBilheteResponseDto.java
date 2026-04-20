package com.teatro.dto;

import java.math.BigDecimal;

public record LugarBilheteResponseDto(
        Integer id,
        Integer lugarId,
        Integer tipoBilheteId,
        BigDecimal precoUnitario
) {
}
