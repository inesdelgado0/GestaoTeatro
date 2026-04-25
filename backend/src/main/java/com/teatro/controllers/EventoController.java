package com.teatro.controllers;

import com.teatro.dto.EventoRequestDto;
import com.teatro.dto.EventoResponseDto;
import com.teatro.entities.Evento;
import com.teatro.services.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    public List<EventoResponseDto> getAll() {
        return eventoService.listarTodos().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/genero/{genero}")
    public List<EventoResponseDto> getByGenero(@PathVariable String genero) {
        return eventoService.procurarPorGenero(genero).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/pesquisa")
    public List<EventoResponseDto> getByTitulo(@RequestParam String termo) {
        return eventoService.procurarPorTitulo(termo).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoResponseDto> getById(@PathVariable Integer id) {
        return eventoService.procurarPorId(id)
                .map(this::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EventoResponseDto> create(@RequestBody EventoRequestDto request) {
        Evento evento = Evento.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .duracaoMin(request.duracaoMin())
                .classificacaoEtaria(request.classificacaoEtaria())
                .genero(request.genero())
                .build();

        return ResponseEntity.ok(toResponseDto(eventoService.criarEvento(evento)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponseDto> update(@PathVariable Integer id, @RequestBody EventoRequestDto request) {
        Evento evento = Evento.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .duracaoMin(request.duracaoMin())
                .classificacaoEtaria(request.classificacaoEtaria())
                .genero(request.genero())
                .build();

        return ResponseEntity.ok(toResponseDto(eventoService.atualizarEvento(id, evento)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventoService.eliminarEvento(id);
        return ResponseEntity.noContent().build();
    }

    private EventoResponseDto toResponseDto(Evento evento) {
        return new EventoResponseDto(
                evento.getId(),
                evento.getTitulo(),
                evento.getDescricao(),
                evento.getDuracaoMin(),
                evento.getClassificacaoEtaria(),
                evento.getGenero()
        );
    }
}
