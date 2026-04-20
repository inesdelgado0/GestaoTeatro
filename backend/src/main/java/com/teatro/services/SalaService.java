package com.teatro.services;

import com.teatro.entities.Sala;
import com.teatro.repositories.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalaService {

    private final SalaRepository salaRepository;

    public List<Sala> listarTodas() {
        return salaRepository.findAll();
    }

    public Optional<Sala> procurarPorId(Integer id) {
        return salaRepository.findById(id);
    }

    public Sala criarSala(Sala sala) {
        if (sala.getNome() == null || sala.getNome().isBlank()) {
            throw new RuntimeException("O nome da sala e obrigatorio.");
        }

        if (sala.getCapacidadeTotal() == null || sala.getCapacidadeTotal() <= 0) {
            throw new RuntimeException("A capacidade total da sala tem de ser superior a zero.");
        }

        if (salaRepository.existsByNomeIgnoreCase(sala.getNome())) {
            throw new RuntimeException("Ja existe uma sala com esse nome.");
        }

        return salaRepository.save(sala);
    }
}
