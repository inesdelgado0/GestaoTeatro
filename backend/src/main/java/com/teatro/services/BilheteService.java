package com.teatro.services;

import com.teatro.entities.Bilhete;
import com.teatro.entities.LugarBilhete;
import com.teatro.entities.Sessao;
import com.teatro.repositories.BilheteRepository;
import com.teatro.repositories.LugarBilheteRepository;
import com.teatro.repositories.SessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BilheteService {

    private final BilheteRepository bilheteRepository;
    private final LugarBilheteRepository lugarBilheteRepository;
    private final SessaoRepository sessaoRepository;

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

    @Transactional
    public Bilhete criarBilhete(Bilhete bilhete) {
        if (bilhete.getSessao() == null) {
            throw new RuntimeException("O bilhete tem de estar associado a uma sessão.");
        }

        if (bilhete.getSessao().getId() == null) {
            throw new RuntimeException("A sessão associada ao bilhete tem de existir.");
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

        Sessao sessao = sessaoRepository.findWithLockById(bilhete.getSessao().getId())
                .orElseThrow(() -> new RuntimeException("A sessão associada ao bilhete tem de existir."));
        bilhete.setSessao(sessao);

        BigDecimal precoTotal = BigDecimal.ZERO;
        Set<Integer> lugaresNoPedido = new HashSet<>();

        for (LugarBilhete lugarBilhete : bilhete.getLugarBilhetes()) {
            if (lugarBilhete.getLugar() == null) {
                throw new RuntimeException("Cada lugar bilhete tem de estar associado a um lugar.");
            }

            if (lugarBilhete.getLugar().getId() == null) {
                throw new RuntimeException("O lugar associado ao lugar bilhete tem de existir.");
            }

            if (!lugaresNoPedido.add(lugarBilhete.getLugar().getId())) {
                throw new RuntimeException("O mesmo lugar não pode ser selecionado mais do que uma vez na mesma compra.");
            }

            if (lugarBilhete.getTipoBilhete() == null) {
                throw new RuntimeException("Cada lugar bilhete tem de ter um tipo de bilhete.");
            }

            if (lugarBilhete.getTipoBilhete().getId() == null) {
                throw new RuntimeException("O tipo de bilhete associado ao lugar bilhete tem de existir.");
            }

            if (lugarBilheteRepository.existsByLugarIdAndSessaoId(
                    lugarBilhete.getLugar().getId(),
                    sessao.getId()
            )) {
                throw new RuntimeException("Lugar já ocupado para esta sessão.");
            }

            if (lugarBilhete.getPrecoUnitario() == null) {
                throw new RuntimeException("Cada lugar bilhete tem de ter preço unitário definido.");
            }

            lugarBilhete.setBilhete(bilhete);
            precoTotal = precoTotal.add(lugarBilhete.getPrecoUnitario());
        }

        bilhete.setPrecoFinal(precoTotal);

        return bilheteRepository.save(bilhete);
    }
}
