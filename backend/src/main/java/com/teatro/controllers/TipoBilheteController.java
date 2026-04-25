package com.teatro.controllers;

import com.teatro.dto.TipoBilheteRequestDto;
import com.teatro.dto.TipoBilheteResponseDto;
import com.teatro.entities.Tipobilhete;
import com.teatro.services.TipoBilheteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-bilhete")
@RequiredArgsConstructor
public class TipoBilheteController {

    private final TipoBilheteService tipoBilheteService;

    @GetMapping
    public List<TipoBilheteResponseDto> getAll() {
        return tipoBilheteService.listarTodos().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoBilheteResponseDto> getById(@PathVariable Integer id) {
        return tipoBilheteService.procurarPorId(id)
                .map(this::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TipoBilheteResponseDto> create(@RequestBody TipoBilheteRequestDto request) {
        Tipobilhete tipoBilhete = Tipobilhete.builder()
                .nome(request.nome())
                .percentagemDesconto(request.percentagemDesconto())
                .build();

        return ResponseEntity.ok(toResponseDto(tipoBilheteService.criarTipoBilhete(tipoBilhete)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoBilheteResponseDto> update(@PathVariable Integer id, @RequestBody TipoBilheteRequestDto request) {
        Tipobilhete tipoBilhete = Tipobilhete.builder()
                .nome(request.nome())
                .percentagemDesconto(request.percentagemDesconto())
                .build();

        return ResponseEntity.ok(toResponseDto(tipoBilheteService.atualizarTipoBilhete(id, tipoBilhete)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        tipoBilheteService.eliminarTipoBilhete(id);
        return ResponseEntity.noContent().build();
    }

    private TipoBilheteResponseDto toResponseDto(Tipobilhete tipoBilhete) {
        return new TipoBilheteResponseDto(
                tipoBilhete.getId(),
                tipoBilhete.getNome(),
                tipoBilhete.getPercentagemDesconto()
        );
    }
}
