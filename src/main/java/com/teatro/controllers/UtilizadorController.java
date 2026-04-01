package com.teatro.controllers;

import com.teatro.dto.UtilizadorRequestDto;
import com.teatro.dto.UtilizadorResponseDto;
import com.teatro.entities.Utilizador;
import com.teatro.services.UtilizadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilizadores")
@RequiredArgsConstructor
public class UtilizadorController {

    private final UtilizadorService utilizadorService;

    @GetMapping
    public List<UtilizadorResponseDto> getAll() {
        return utilizadorService.listarTodos().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilizadorResponseDto> getById(@PathVariable Integer id) {
        return utilizadorService.procurarPorId(id)
                .map(this::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/registo")
    public ResponseEntity<UtilizadorResponseDto> registarCliente(@RequestBody UtilizadorRequestDto request) {
        Utilizador utilizador = Utilizador.builder()
                .nome(request.nome())
                .email(request.email())
                .password(request.password())
                .telemovel(request.telemovel())
                .morada(request.morada())
                .nif(request.nif())
                .build();

        Utilizador guardado = utilizadorService.registarCliente(utilizador);
        return ResponseEntity.ok(toResponseDto(guardado));
    }

    private UtilizadorResponseDto toResponseDto(Utilizador utilizador) {
        return new UtilizadorResponseDto(
                utilizador.getId(),
                utilizador.getNome(),
                utilizador.getEmail(),
                utilizador.getTelemovel(),
                utilizador.getMorada(),
                utilizador.getNif(),
                utilizador.getTipoUtilizador() != null ? utilizador.getTipoUtilizador().getTipo() : null
        );
    }
}
