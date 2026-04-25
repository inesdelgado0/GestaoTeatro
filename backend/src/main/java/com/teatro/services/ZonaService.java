package com.teatro.services;

import com.teatro.entities.Zona;
import com.teatro.repositories.LugarRepository;
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
    private final LugarRepository lugarRepository;

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
        validarZona(zona);
        return zonaRepository.save(zona);
    }

    public Zona atualizarZona(Integer id, Zona zonaAtualizada) {
        Zona zonaExistente = zonaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zona nao encontrada."));

        validarZona(zonaAtualizada);

        zonaExistente.setNome(zonaAtualizada.getNome());
        zonaExistente.setTaxaAdicional(zonaAtualizada.getTaxaAdicional());
        zonaExistente.setSala(zonaAtualizada.getSala());

        return zonaRepository.save(zonaExistente);
    }

    public void eliminarZona(Integer id) {
        if (!zonaRepository.existsById(id)) {
            throw new RuntimeException("Nao e possivel apagar: zona nao encontrada.");
        }

        if (lugarRepository.existsByZonaId(id)) {
            throw new RuntimeException("Nao e possivel apagar a zona porque existem lugares associados.");
        }
        zonaRepository.deleteById(id);
    }

    private void validarZona(Zona zona) {
        if (zona.getNome() == null || zona.getNome().isBlank()) {
            throw new RuntimeException("O nome da zona e obrigatorio.");
        }

        if (zona.getSala() == null) {
            throw new RuntimeException("A zona tem de estar associada a uma sala.");
        }

        if (zona.getTaxaAdicional() != null && zona.getTaxaAdicional().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("A taxa adicional da zona nao pode ser negativa.");
        }
    }
}
