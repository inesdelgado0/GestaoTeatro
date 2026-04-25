package com.teatro.controllers;

import com.teatro.dto.LugarRequestDto;
import com.teatro.dto.LugarResponseDto;
import com.teatro.entities.Lugar;
import com.teatro.entities.Zona;
import com.teatro.services.LugarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lugares")
@RequiredArgsConstructor
public class LugarController {

    private final LugarService lugarService;

    @GetMapping
    public List<LugarResponseDto> getAll() {
        return lugarService.listarTodos().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LugarResponseDto> getById(@PathVariable Integer id) {
        return lugarService.procurarPorId(id)
                .map(this::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/zona/{zonaId}")
    public List<LugarResponseDto> getByZona(@PathVariable Integer zonaId) {
        return lugarService.listarPorZona(zonaId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<LugarResponseDto> create(@RequestBody LugarRequestDto request) {
        Lugar lugar = Lugar.builder()
                .fila(request.fila())
                .numero(request.numero())
                .zona(request.zonaId() != null ? Zona.builder().id(request.zonaId()).build() : null)
                .build();

        return ResponseEntity.ok(toResponseDto(lugarService.criarLugar(lugar)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LugarResponseDto> update(@PathVariable Integer id, @RequestBody LugarRequestDto request) {
        Lugar lugar = Lugar.builder()
                .fila(request.fila())
                .numero(request.numero())
                .zona(request.zonaId() != null ? Zona.builder().id(request.zonaId()).build() : null)
                .build();

        return ResponseEntity.ok(toResponseDto(lugarService.atualizarLugar(id, lugar)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        lugarService.eliminarLugar(id);
        return ResponseEntity.noContent().build();
    }

    private LugarResponseDto toResponseDto(Lugar lugar) {
        return new LugarResponseDto(
                lugar.getId(),
                lugar.getFila(),
                lugar.getNumero(),
                lugar.getZona() != null ? lugar.getZona().getId() : null
        );
    }
}
