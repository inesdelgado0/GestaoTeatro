package com.teatro.services;

import com.teatro.entities.Tipobilhete;
import com.teatro.repositories.LugarBilheteRepository;
import com.teatro.repositories.TipoBilheteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TipoBilheteService {

    private final TipoBilheteRepository tipoBilheteRepository;
    private final LugarBilheteRepository lugarBilheteRepository;

    public List<Tipobilhete> listarTodos() {
        return tipoBilheteRepository.findAll();
    }

    public Optional<Tipobilhete> procurarPorId(Integer id) {
        return tipoBilheteRepository.findById(id);
    }

    public Tipobilhete criarTipoBilhete(Tipobilhete tipoBilhete) {
        validarTipoBilhete(tipoBilhete, null);
        return tipoBilheteRepository.save(tipoBilhete);
    }

    public Tipobilhete atualizarTipoBilhete(Integer id, Tipobilhete tipoBilheteAtualizado) {
        Tipobilhete existente = tipoBilheteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria de desconto nao encontrada."));

        validarTipoBilhete(tipoBilheteAtualizado, id);

        existente.setNome(tipoBilheteAtualizado.getNome());
        existente.setPercentagemDesconto(tipoBilheteAtualizado.getPercentagemDesconto());

        return tipoBilheteRepository.save(existente);
    }

    public void eliminarTipoBilhete(Integer id) {
        if (!tipoBilheteRepository.existsById(id)) {
            throw new RuntimeException("Nao e possivel apagar: categoria de desconto nao encontrada.");
        }

        if (lugarBilheteRepository.existsByTipoBilheteId(id)) {
            throw new RuntimeException("Nao e possivel apagar a categoria porque existem bilhetes associados.");
        }

        tipoBilheteRepository.deleteById(id);
    }

    private void validarTipoBilhete(Tipobilhete tipoBilhete, Integer idIgnorado) {
        if (tipoBilhete.getNome() == null || tipoBilhete.getNome().isBlank()) {
            throw new RuntimeException("O nome da categoria e obrigatorio.");
        }

        if (tipoBilhete.getPercentagemDesconto() == null) {
            throw new RuntimeException("A percentagem de desconto e obrigatoria.");
        }

        if (tipoBilhete.getPercentagemDesconto().compareTo(BigDecimal.ZERO) < 0
                || tipoBilhete.getPercentagemDesconto().compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("A percentagem de desconto tem de estar entre 0 e 100.");
        }

        tipoBilheteRepository.findByNomeIgnoreCase(tipoBilhete.getNome())
                .filter(existente -> !existente.getId().equals(idIgnorado))
                .ifPresent(existente -> {
                    throw new RuntimeException("Ja existe uma categoria com esse nome.");
                });
    }
}
