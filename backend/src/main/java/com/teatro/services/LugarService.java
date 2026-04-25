package com.teatro.services;

import com.teatro.entities.Lugar;
import com.teatro.entities.Zona;
import com.teatro.repositories.LugarBilheteRepository;
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
    private final LugarBilheteRepository lugarBilheteRepository;

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
        Zona zona = validarLugar(lugar, null);
        lugar.setZona(zona);
        return lugarRepository.save(lugar);
    }

    public Lugar atualizarLugar(Integer id, Lugar lugarAtualizado) {
        Lugar lugarExistente = lugarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lugar nao encontrado."));

        Zona zona = validarLugar(lugarAtualizado, id);

        lugarExistente.setFila(lugarAtualizado.getFila());
        lugarExistente.setNumero(lugarAtualizado.getNumero());
        lugarExistente.setZona(zona);

        return lugarRepository.save(lugarExistente);
    }

    public void eliminarLugar(Integer id) {
        if (!lugarRepository.existsById(id)) {
            throw new RuntimeException("Nao e possivel apagar: lugar nao encontrado.");
        }

        if (lugarBilheteRepository.existsByLugarId(id)) {
            throw new RuntimeException("Nao e possivel apagar o lugar porque existem bilhetes associados.");
        }

        lugarRepository.deleteById(id);
    }

    private Zona validarLugar(Lugar lugar, Integer lugarIdIgnorado) {
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
        ).filter(lugarExistente -> !lugarExistente.getId().equals(lugarIdIgnorado)).isPresent()) {
            throw new RuntimeException("Ja existe um lugar com essa fila e numero nessa sala.");
        }

        return zona;
    }
}
