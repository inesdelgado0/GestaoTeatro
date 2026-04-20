package com.teatro.services;

import com.teatro.entities.Zona;
import com.teatro.repositories.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ZonaService {

    private final ZonaRepository zonaRepository;

    public List<Zona> listarTodas() {
        return zonaRepository.findAll();
    }

    public Optional<Zona> procurarPorId(Integer id) {
        return zonaRepository.findById(id);
    }

    public List<Zona> listarPorSala(Integer salaId) {
        return zonaRepository.findBySalaId(salaId);
    }

    public Zona criarZona(Zona zona) {
        if (zona.getNome() == null || zona.getNome().isBlank()) {
            throw new RuntimeException("O nome da zona e obrigatorio.");
        }

        if (zona.getSala() == null) {
            throw new RuntimeException("A zona tem de estar associada a uma sala.");
        }

        if (zona.getTaxaAdicional() != null && zona.getTaxaAdicional().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("A taxa adicional da zona nao pode ser negativa.");
        }

        return zonaRepository.save(zona);
    }
}
