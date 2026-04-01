package com.teatro.dto;

import java.util.List;

public record BilheteRequestDto(
        Integer sessaoId,
        Integer utilizadorId,
        Integer pagamentoId,
        List<LugarBilheteRequestDto> lugarBilhetes
) {
}
