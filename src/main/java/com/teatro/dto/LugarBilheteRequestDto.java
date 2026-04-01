package com.teatro.dto;

import java.math.BigDecimal;

public record LugarBilheteRequestDto(
        Integer lugarId,
        Integer tipoBilheteId,
        BigDecimal precoUnitario
) {
}
