package com.teatro.services;

import com.teatro.entities.Bilhete;
import com.teatro.entities.LugarBilhete;
import com.teatro.repositories.BilheteRepository;
import com.teatro.repositories.LugarBilheteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BilheteService {

    private final BilheteRepository bilheteRepository;
    private final LugarBilheteRepository lugarBilheteRepository;

    public List<Bilhete> listarTodos() {
        return bilheteRepository.findAll();
    }

    public Optional<Bilhete> procurarPorId(Integer id) {
        return bilheteRepository.findById(id);
    }

    public List<Bilhete> listarPorSessao(Integer sessaoId) {
        return bilheteRepository.findBySessaoId(sessaoId);
    }

    public List<Bilhete> listarPorUtilizador(Integer utilizadorId) {
        return bilheteRepository.findByUtilizadorId(utilizadorId);
    }

    public Bilhete criarBilhete(Bilhete bilhete) {
        if (bilhete.getSessao() == null) {
            throw new RuntimeException("O bilhete tem de estar associado a uma sessao.");
        }

        if (bilhete.getSessao().getId() == null) {
            throw new RuntimeException("A sessao associada ao bilhete tem de existir.");
        }

        if (bilhete.getUtilizador() == null) {
            throw new RuntimeException("O bilhete tem de estar associado a um utilizador.");
        }

        if (bilhete.getUtilizador().getId() == null) {
            throw new RuntimeException("O utilizador associado ao bilhete tem de existir.");
        }

        if (bilhete.getLugarBilhetes() == null || bilhete.getLugarBilhetes().isEmpty()) {
            throw new RuntimeException("A compra tem de incluir pelo menos um lugar.");
        }

        BigDecimal precoTotal = BigDecimal.ZERO;

        for (LugarBilhete lugarBilhete : bilhete.getLugarBilhetes()) {
            if (lugarBilhete.getLugar() == null) {
                throw new RuntimeException("Cada lugar bilhete tem de estar associado a um lugar.");
            }

            if (lugarBilhete.getLugar().getId() == null) {
                throw new RuntimeException("O lugar associado ao lugar bilhete tem de existir.");
            }

            if (lugarBilhete.getTipoBilhete() == null) {
                throw new RuntimeException("Cada lugar bilhete tem de ter um tipo de bilhete.");
            }

            if (lugarBilhete.getTipoBilhete().getId() == null) {
                throw new RuntimeException("O tipo de bilhete associado ao lugar bilhete tem de existir.");
            }

            if (lugarBilheteRepository.existsByLugarIdAndSessaoId(
                    lugarBilhete.getLugar().getId(),
                    bilhete.getSessao().getId()
            )) {
                throw new RuntimeException("Lugar ja ocupado para esta sessao.");
            }

            if (lugarBilhete.getPrecoUnitario() == null) {
                throw new RuntimeException("Cada lugar bilhete tem de ter preco unitario definido.");
            }

            lugarBilhete.setBilhete(bilhete);
            precoTotal = precoTotal.add(lugarBilhete.getPrecoUnitario());
        }

        bilhete.setPrecoFinal(precoTotal);

        return bilheteRepository.save(bilhete);
    }
}
