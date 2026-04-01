package com.teatro.controllers;

import com.teatro.dto.SalaRequestDto;
import com.teatro.dto.SalaResponseDto;
import com.teatro.entities.Sala;
import com.teatro.services.SalaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salas")
@RequiredArgsConstructor
public class SalaController {

    private final SalaService salaService;

    @GetMapping
    public List<SalaResponseDto> getAll() {
        return salaService.listarTodas().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaResponseDto> getById(@PathVariable Integer id) {
        return salaService.procurarPorId(id)
                .map(this::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SalaResponseDto> create(@RequestBody SalaRequestDto request) {
        Sala sala = Sala.builder()
                .nome(request.nome())
                .capacidadeTotal(request.capacidadeTotal())
                .build();

        return ResponseEntity.ok(toResponseDto(salaService.criarSala(sala)));
    }

    private SalaResponseDto toResponseDto(Sala sala) {
        return new SalaResponseDto(
                sala.getId(),
                sala.getNome(),
                sala.getCapacidadeTotal()
        );
    }
}
