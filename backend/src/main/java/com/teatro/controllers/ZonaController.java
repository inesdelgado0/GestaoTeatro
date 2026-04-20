package com.teatro.controllers;

import com.teatro.dto.ZonaRequestDto;
import com.teatro.dto.ZonaResponseDto;
import com.teatro.entities.Sala;
import com.teatro.entities.Zona;
import com.teatro.services.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
public class ZonaController {

    private final ZonaService zonaService;

    @GetMapping
    public List<ZonaResponseDto> getAll() {
        return zonaService.listarTodas().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ZonaResponseDto> getById(@PathVariable Integer id) {
        return zonaService.procurarPorId(id)
                .map(this::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sala/{salaId}")
    public List<ZonaResponseDto> getBySala(@PathVariable Integer salaId) {
        return zonaService.listarPorSala(salaId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ZonaResponseDto> create(@RequestBody ZonaRequestDto request) {
        Zona zona = Zona.builder()
                .nome(request.nome())
                .taxaAdicional(request.taxaAdicional())
                .sala(request.salaId() != null ? Sala.builder().id(request.salaId()).build() : null)
                .build();

        return ResponseEntity.ok(toResponseDto(zonaService.criarZona(zona)));
    }

    private ZonaResponseDto toResponseDto(Zona zona) {
        return new ZonaResponseDto(
                zona.getId(),
                zona.getNome(),
                zona.getTaxaAdicional(),
                zona.getSala() != null ? zona.getSala().getId() : null
        );
    }
}
