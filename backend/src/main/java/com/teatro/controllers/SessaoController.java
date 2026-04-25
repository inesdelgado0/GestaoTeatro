package com.teatro.controllers;

import com.teatro.dto.SessaoRequestDto;
import com.teatro.dto.SessaoResponseDto;
import com.teatro.entities.Evento;
import com.teatro.entities.Sessao;
import com.teatro.entities.Sala;
import com.teatro.services.SessaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessoes")
@RequiredArgsConstructor
public class SessaoController {

    private final SessaoService sessaoService;

    @GetMapping
    public List<SessaoResponseDto> getAll() {
        return sessaoService.listarTodas().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessaoResponseDto> getById(@PathVariable Integer id) {
        return sessaoService.procurarPorId(id)
                .map(this::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/evento/{eventoId}")
    public List<SessaoResponseDto> getByEvento(@PathVariable Integer eventoId) {
        return sessaoService.listarPorEvento(eventoId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<SessaoResponseDto> create(@RequestBody SessaoRequestDto request) {
        Sessao sessao = Sessao.builder()
                .dataHora(request.dataHora())
                .precoBase(request.precoBase())
                .estado(request.estado())
                .evento(request.eventoId() != null ? Evento.builder().id(request.eventoId()).build() : null)
                .sala(request.salaId() != null ? Sala.builder().id(request.salaId()).build() : null)
                .build();

        return ResponseEntity.ok(toResponseDto(sessaoService.criarSessao(sessao)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessaoResponseDto> update(@PathVariable Integer id, @RequestBody SessaoRequestDto request) {
        Sessao sessao = Sessao.builder()
                .dataHora(request.dataHora())
                .precoBase(request.precoBase())
                .estado(request.estado())
                .evento(request.eventoId() != null ? Evento.builder().id(request.eventoId()).build() : null)
                .sala(request.salaId() != null ? Sala.builder().id(request.salaId()).build() : null)
                .build();

        return ResponseEntity.ok(toResponseDto(sessaoService.atualizarSessao(id, sessao)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        sessaoService.eliminarSessao(id);
        return ResponseEntity.noContent().build();
    }

    private SessaoResponseDto toResponseDto(Sessao sessao) {
        return new SessaoResponseDto(
                sessao.getId(),
                sessao.getDataHora(),
                sessao.getPrecoBase(),
                sessao.getEstado() != null ? sessao.getEstado().name() : null,
                sessao.getEvento() != null ? sessao.getEvento().getId() : null,
                sessao.getSala() != null ? sessao.getSala().getId() : null
        );
    }
}
