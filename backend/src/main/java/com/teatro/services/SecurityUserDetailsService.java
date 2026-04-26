package com.teatro.services;

import com.teatro.entities.Utilizador;
import com.teatro.repositories.UtilizadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SecurityUserDetailsService implements UserDetailsService {

    private final UtilizadorRepository utilizadorRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilizador utilizador = utilizadorRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado."));

        String tipo = utilizador.getTipoUtilizador() != null ? utilizador.getTipoUtilizador().getTipo() : "Cliente";

        return User.withUsername(utilizador.getEmail())
                .password(utilizador.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(toAuthority(tipo))))
                .build();
    }

    private String toAuthority(String tipoUtilizador) {
        String normalizado = tipoUtilizador == null
                ? "CLIENTE"
                : tipoUtilizador
                .trim()
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);

        return "ROLE_" + normalizado;
    }
}
