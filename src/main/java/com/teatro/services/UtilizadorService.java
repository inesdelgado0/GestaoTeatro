package com.teatro.services;

import com.teatro.entities.Tipoutilizador;
import com.teatro.entities.Utilizador;
import com.teatro.repositories.TipoUtilizadorRepository;
import com.teatro.repositories.UtilizadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UtilizadorService {

    private final UtilizadorRepository utilizadorRepository;
    private final TipoUtilizadorRepository tipoUtilizadorRepository;

    public List<Utilizador> listarTodos() {
        return utilizadorRepository.findAll();
    }

    public Optional<Utilizador> procurarPorId(Integer id) {
        return utilizadorRepository.findById(id);
    }

    public Optional<Utilizador> procurarPorEmail(String email) {
        return utilizadorRepository.findByEmail(email);
    }

    public Utilizador registarCliente(Utilizador utilizador) {
        if (utilizador.getNome() == null || utilizador.getNome().isBlank()) {
            throw new RuntimeException("O nome do utilizador e obrigatorio.");
        }

        if (utilizador.getEmail() == null || utilizador.getEmail().isBlank()) {
            throw new RuntimeException("O email do utilizador e obrigatorio.");
        }

        if (utilizador.getPassword() == null || utilizador.getPassword().isBlank()) {
            throw new RuntimeException("A password do utilizador e obrigatoria.");
        }

        if (utilizadorRepository.existsByEmail(utilizador.getEmail())) {
            throw new RuntimeException("Ja existe um utilizador com esse email.");
        }

        Tipoutilizador tipoCliente = tipoUtilizadorRepository.findByTipoIgnoreCase("Cliente")
                .orElseThrow(() -> new RuntimeException("Tipo de utilizador 'Cliente' nao encontrado."));

        utilizador.setTipoUtilizador(tipoCliente);

        return utilizadorRepository.save(utilizador);
    }
}
