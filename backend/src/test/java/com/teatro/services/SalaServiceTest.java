package com.teatro.services;

import com.teatro.entities.Sala;
import com.teatro.repositories.LugarRepository;
import com.teatro.repositories.SalaRepository;
import com.teatro.repositories.SessaoRepository;
import com.teatro.repositories.ZonaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalaServiceTest {

    @Mock
    private SalaRepository salaRepository;

    @Mock
    private ZonaRepository zonaRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private LugarRepository lugarRepository;

    @InjectMocks
    private SalaService salaService;

    @Test
    void atualizarSalaDeveFalharQuandoCapacidadeFicaAbaixoDosLugaresConfigurados() {
        when(salaRepository.findById(4)).thenReturn(Optional.of(Sala.builder().id(4).nome("Sala Principal").capacidadeTotal(120).build()));
        when(salaRepository.findByNomeIgnoreCase("Sala Principal")).thenReturn(Optional.of(Sala.builder().id(4).nome("Sala Principal").capacidadeTotal(120).build()));
        when(lugarRepository.countByZonaSalaId(4)).thenReturn(90L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> salaService.atualizarSala(4, Sala.builder().nome("Sala Principal").capacidadeTotal(80).build()));

        assertEquals("A capacidade total não pode ser inferior ao número de lugares configurados.", exception.getMessage());
    }

    @Test
    void eliminarSalaDeveFalharQuandoExistemZonasAssociadas() {
        when(salaRepository.existsById(5)).thenReturn(true);
        when(zonaRepository.existsBySalaId(5)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> salaService.eliminarSala(5));

        assertEquals("Não é possível apagar a sala porque existem zonas associadas.", exception.getMessage());
        verify(salaRepository, never()).deleteById(5);
    }
}
