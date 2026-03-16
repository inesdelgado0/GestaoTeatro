package com.teatro.controllers;

import com.teatro.entities.Evento;
import com.teatro.services.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos") // O link será http://localhost:8080/api/eventos
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    // Listar todos (GET)
    @GetMapping
    public List<Evento> getAll() {
        return eventoService.listarTodos();
    }

    // Criar um novo (POST)
    @PostMapping("/add")
    public ResponseEntity<Evento> create(@RequestBody Evento evento) {
        return ResponseEntity.ok(eventoService.criarEvento(evento));
    }

    // Procurar por ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Evento> getById(@PathVariable Integer id) {
        return eventoService.procurarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}