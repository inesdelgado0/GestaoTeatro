package com.teatro.dto;

public record AuthLoginRequestDto(
        String email,
        String password
) {
}
