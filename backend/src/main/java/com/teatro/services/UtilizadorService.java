package com.teatro.services;

import com.teatro.entities.Tipoutilizador;
import com.teatro.entities.Utilizador;
import com.teatro.repositories.TipoUtilizadorRepository;
import com.teatro.repositories.UtilizadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UtilizadorService {

    private final UtilizadorRepository utilizadorRepository;
    private final TipoUtilizadorRepository tipoUtilizadorRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Utilizador> listarTodos() {
        return utilizadorRepository.findAll();
    }

    public Optional<Utilizador> procurarPorId(Integer id) {
        return utilizadorRepository.findById(id);
    }

    public Optional<Utilizador> procurarPorEmail(String email) {
        return utilizadorRepository.findByEmail(email);
    }

    public Utilizador autenticarAdministrador(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("O email é obrigatório.");
        }

        if (password == null || password.isBlank()) {
            throw new RuntimeException("A password é obrigatória.");
        }

        Utilizador utilizador = utilizadorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas."));

        if (!isPasswordHashed(utilizador.getPassword())) {
            throw new RuntimeException("A conta requer reposição segura da password antes de iniciar sessão.");
        }

        if (!passwordEncoder.matches(password, utilizador.getPassword())) {
            throw new RuntimeException("Credenciais inválidas.");
        }

        if (utilizador.getTipoUtilizador() == null
                || utilizador.getTipoUtilizador().getTipo() == null
                || !utilizador.getTipoUtilizador().getTipo().equalsIgnoreCase("Administrador")) {
            throw new RuntimeException("O utilizador não tem permissão administrativa.");
        }

        return utilizador;
    }

    public Utilizador registarCliente(Utilizador utilizador) {
        if (utilizador.getNome() == null || utilizador.getNome().isBlank()) {
            throw new RuntimeException("O nome do utilizador é obrigatório.");
        }

        if (utilizador.getEmail() == null || utilizador.getEmail().isBlank()) {
            throw new RuntimeException("O email do utilizador é obrigatório.");
        }

        if (utilizador.getPassword() == null || utilizador.getPassword().isBlank()) {
            throw new RuntimeException("A password do utilizador é obrigatória.");
        }

        if (utilizadorRepository.existsByEmail(utilizador.getEmail())) {
            throw new RuntimeException("Já existe um utilizador com esse email.");
        }

        Tipoutilizador tipoCliente = tipoUtilizadorRepository.findByTipoIgnoreCase("Cliente")
                .orElseThrow(() -> new RuntimeException("Tipo de utilizador 'Cliente' não encontrado."));

        utilizador.setTipoUtilizador(tipoCliente);
        utilizador.setPassword(passwordEncoder.encode(utilizador.getPassword()));

        return utilizadorRepository.save(utilizador);
    }

    private boolean isPasswordHashed(String password) {
        return password != null
                && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }
}
