package com.teatro.desktop.model;

public record AuthLoginResponse(
        Integer id,
        String nome,
        String email,
        String tipoUtilizador
) {
}
