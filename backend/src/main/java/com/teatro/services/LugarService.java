package com.teatro.services;

import com.teatro.entities.Lugar;
import com.teatro.repositories.LugarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LugarService {

    private final LugarRepository lugarRepository;

    public List<Lugar> listarTodos() {
        return lugarRepository.findAll();
    }

    public Optional<Lugar> procurarPorId(Integer id) {
        return lugarRepository.findById(id);
    }

    public List<Lugar> listarPorZona(Integer zonaId) {
        return lugarRepository.findByZonaIdOrderByFilaAscNumeroAsc(zonaId);
    }

    public Lugar criarLugar(Lugar lugar) {
        if (lugar.getZona() == null) {
            throw new RuntimeException("O lugar tem de estar associado a uma zona.");
        }

        if (lugar.getFila() == null || lugar.getFila().isBlank()) {
            throw new RuntimeException("A fila do lugar e obrigatoria.");
        }

        if (lugar.getNumero() == null || lugar.getNumero() <= 0) {
            throw new RuntimeException("O numero do lugar tem de ser superior a zero.");
        }

        if (lugarRepository.findByZonaIdAndFilaAndNumero(
                lugar.getZona().getId(),
                lugar.getFila(),
                lugar.getNumero()
        ).isPresent()) {
            throw new RuntimeException("Ja existe um lugar com essa fila e numero nessa zona.");
        }

        return lugarRepository.save(lugar);
    }
}
