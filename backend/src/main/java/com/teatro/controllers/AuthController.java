package com.teatro.controllers;

import com.teatro.dto.AuthLoginRequestDto;
import com.teatro.dto.AuthLoginResponseDto;
import com.teatro.entities.Utilizador;
import com.teatro.services.UtilizadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtilizadorService utilizadorService;

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponseDto> login(@RequestBody AuthLoginRequestDto request) {
        Utilizador utilizador = utilizadorService.autenticarAdministrador(request.email(), request.password());

        return ResponseEntity.ok(new AuthLoginResponseDto(
                utilizador.getId(),
                utilizador.getNome(),
                utilizador.getEmail(),
                utilizador.getTipoUtilizador().getTipo()
        ));
    }
}
