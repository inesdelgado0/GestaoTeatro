package com.teatro.services;

import com.teatro.entities.Evento;
import com.teatro.repositories.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    public Optional<Evento> procurarPorId(Integer id) {
        return eventoRepository.findById(id);
    }

    public Evento criarEvento(Evento evento) {
        // Exemplo de Regra de Negócio: O título não pode ser vazio
        if (evento.getTitulo() == null || evento.getTitulo().isBlank()) {
            throw new RuntimeException("O título do evento não pode estar vazio!");
        }
        return eventoRepository.save(evento);
    }
    public void eliminarEvento(Integer id) {
        if (eventoRepository.existsById(id)) {
            eventoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Não é possível apagar: Evento não encontrado.");
        }
    }
}