package com.teatro.services;

import com.teatro.entities.Evento;
import com.teatro.repositories.EventoRepository;
import com.teatro.repositories.SessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final SessaoRepository sessaoRepository;

    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    public Optional<Evento> procurarPorId(Integer id) {
        return eventoRepository.findById(id);
    }

    public List<Evento> procurarPorGenero(String genero) {
        return eventoRepository.findByGenero(genero);
    }

    public List<Evento> procurarPorTitulo(String termo) {
        return eventoRepository.findByTituloContainingIgnoreCase(termo);
    }

    public Evento criarEvento(Evento evento) {
        if (evento.getTitulo() == null || evento.getTitulo().isBlank()) {
            throw new RuntimeException("O titulo do evento nao pode estar vazio.");
        }

        if (eventoRepository.findByTitulo(evento.getTitulo()).isPresent()) {
            throw new RuntimeException("Ja existe um evento com esse titulo.");
        }

        return eventoRepository.save(evento);
    }

    public void eliminarEvento(Integer id) {
        if (!eventoRepository.existsById(id)) {
            throw new RuntimeException("Nao e possivel apagar: evento nao encontrado.");
        }

        if (sessaoRepository.existsByEventoId(id)) {
            throw new RuntimeException("Nao e possivel apagar o evento porque existem sessoes associadas.");
        }

        eventoRepository.deleteById(id);
    }
}
