package com.teatro.dto;

import java.math.BigDecimal;
import java.util.List;

public record BilheteResponseDto(
        Integer id,
        Integer sessaoId,
        Integer utilizadorId,
        Integer pagamentoId,
        String estado,
        BigDecimal precoFinal,
        List<LugarBilheteResponseDto> lugarBilhetes
) {
}
