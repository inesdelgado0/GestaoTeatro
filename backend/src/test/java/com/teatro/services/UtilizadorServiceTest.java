package com.teatro.services;

import com.teatro.config.SecurityConfig;
import com.teatro.entities.Tipoutilizador;
import com.teatro.entities.Utilizador;
import com.teatro.repositories.TipoUtilizadorRepository;
import com.teatro.repositories.UtilizadorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilizadorServiceTest {

    @Mock
    private UtilizadorRepository utilizadorRepository;

    @Mock
    private TipoUtilizadorRepository tipoUtilizadorRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new SecurityConfig().passwordEncoder();

    @InjectMocks
    private UtilizadorService utilizadorService;

    @Test
    void registarClienteDeveGuardarPasswordCodificada() {
        Tipoutilizador tipoCliente = Tipoutilizador.builder().id(2).tipo("Cliente").build();
        Utilizador utilizador = Utilizador.builder()
                .nome("Cliente Teste")
                .email("cliente@teatro.pt")
                .password("segredo")
                .build();

        when(utilizadorRepository.existsByEmail("cliente@teatro.pt")).thenReturn(false);
        when(tipoUtilizadorRepository.findByTipoIgnoreCase("Cliente")).thenReturn(Optional.of(tipoCliente));
        when(utilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador guardado = utilizadorService.registarCliente(utilizador);

        assertNotEquals("segredo", guardado.getPassword());
        assertTrue(passwordEncoder.matches("segredo", guardado.getPassword()));
    }

    @Test
    void autenticarAdministradorDeveRejeitarPasswordEmTextoSimples() {
        Tipoutilizador tipoAdmin = Tipoutilizador.builder().id(1).tipo("Administrador").build();
        Utilizador utilizador = Utilizador.builder()
                .id(1)
                .email("admin@teatro.pt")
                .password("admin123")
                .tipoUtilizador(tipoAdmin)
                .build();

        when(utilizadorRepository.findByEmail("admin@teatro.pt")).thenReturn(Optional.of(utilizador));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> utilizadorService.autenticarAdministrador("admin@teatro.pt", "admin123"));

        assertEquals("A conta requer reposição segura da password antes de iniciar sessão.", exception.getMessage());
        verify(utilizadorRepository, never()).save(any(Utilizador.class));
    }
}
