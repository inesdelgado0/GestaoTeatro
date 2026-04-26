package com.teatro.services;

import com.teatro.entities.Sala;
import com.teatro.repositories.LugarRepository;
import com.teatro.repositories.SalaRepository;
import com.teatro.repositories.SessaoRepository;
import com.teatro.repositories.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalaService {

    private final SalaRepository salaRepository;
    private final ZonaRepository zonaRepository;
    private final SessaoRepository sessaoRepository;
    private final LugarRepository lugarRepository;

    public List<Sala> listarTodas() {
        return salaRepository.findAll();
    }

    public Optional<Sala> procurarPorId(Integer id) {
        return salaRepository.findById(id);
    }

    public Sala criarSala(Sala sala) {
        validarSala(sala, null);
        return salaRepository.save(sala);
    }

    public Sala atualizarSala(Integer id, Sala salaAtualizada) {
        Sala salaExistente = salaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada."));

        validarSala(salaAtualizada, id);

        long lugaresConfigurados = lugarRepository.countByZonaSalaId(id);
        if (salaAtualizada.getCapacidadeTotal() < lugaresConfigurados) {
            throw new RuntimeException("A capacidade total não pode ser inferior ao número de lugares configurados.");
        }

        salaExistente.setNome(salaAtualizada.getNome());
        salaExistente.setCapacidadeTotal(salaAtualizada.getCapacidadeTotal());
        return salaRepository.save(salaExistente);
    }

    public void eliminarSala(Integer id) {
        if (!salaRepository.existsById(id)) {
            throw new RuntimeException("Não é possível apagar: sala não encontrada.");
        }

        if (zonaRepository.existsBySalaId(id)) {
            throw new RuntimeException("Não é possível apagar a sala porque existem zonas associadas.");
        }

        if (sessaoRepository.existsBySalaId(id)) {
            throw new RuntimeException("Não é possível apagar a sala porque existem sessões associadas.");
        }

        salaRepository.deleteById(id);
    }

    private void validarSala(Sala sala, Integer idIgnorado) {
        if (sala.getNome() == null || sala.getNome().isBlank()) {
            throw new RuntimeException("O nome da sala é obrigatório.");
        }

        if (sala.getCapacidadeTotal() == null || sala.getCapacidadeTotal() <= 0) {
            throw new RuntimeException("A capacidade total da sala tem de ser superior a zero.");
        }

        salaRepository.findByNomeIgnoreCase(sala.getNome())
                .filter(existente -> !existente.getId().equals(idIgnorado))
                .ifPresent(existente -> {
                    throw new RuntimeException("Já existe uma sala com esse nome.");
                });
    }
}
