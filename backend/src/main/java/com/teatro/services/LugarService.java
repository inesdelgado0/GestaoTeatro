package com.teatro.services;

import com.teatro.entities.Lugar;
import com.teatro.entities.Zona;
import com.teatro.repositories.LugarRepository;
import com.teatro.repositories.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LugarService {

    private final LugarRepository lugarRepository;
    private final ZonaRepository zonaRepository;

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

        if (lugar.getZona().getId() == null) {
            throw new RuntimeException("A zona do lugar tem de ter um identificador valido.");
        }

        if (lugar.getFila() == null || lugar.getFila().isBlank()) {
            throw new RuntimeException("A fila do lugar e obrigatoria.");
        }

        if (lugar.getNumero() == null || lugar.getNumero() <= 0) {
            throw new RuntimeException("O numero do lugar tem de ser superior a zero.");
        }

        Zona zona = zonaRepository.findById(lugar.getZona().getId())
                .orElseThrow(() -> new RuntimeException("A zona indicada nao existe."));

        if (zona.getSala() == null || zona.getSala().getId() == null) {
            throw new RuntimeException("A zona indicada nao esta associada a uma sala valida.");
        }

        if (lugarRepository.findByZonaSalaIdAndFilaAndNumero(
                zona.getSala().getId(),
                lugar.getFila(),
                lugar.getNumero()
        ).isPresent()) {
            throw new RuntimeException("Ja existe um lugar com essa fila e numero nessa sala.");
        }

        lugar.setZona(zona);
        return lugarRepository.save(lugar);
    }
}
