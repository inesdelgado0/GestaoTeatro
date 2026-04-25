package com.teatro.services;

import com.teatro.entities.EstadoSessao;
import com.teatro.entities.Evento;
import com.teatro.entities.Sala;
import com.teatro.entities.Sessao;
import com.teatro.repositories.BilheteRepository;
import com.teatro.repositories.SessaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessaoServiceTest {

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private BilheteRepository bilheteRepository;

    @InjectMocks
    private SessaoService sessaoService;

    @Test
    void criarSessaoDeveFalharQuandoExisteConflitoDeHorario() {
        Sessao sessao = criarSessao(1, Instant.parse("2026-04-20T20:00:00Z"));
        when(sessaoRepository.findBySalaIdAndDataHoraBetween(any(), any(), any()))
                .thenReturn(List.of(criarSessao(2, Instant.parse("2026-04-20T20:00:00Z"))));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> sessaoService.criarSessao(sessao));

        assertEquals("Ja existe uma sessao agendada para essa sala nesse horario.", exception.getMessage());
    }

    @Test
    void criarSessaoDeveAssumirEstadoAbertaQuandoNaoForDefinido() {
        Sessao sessao = criarSessao(1, Instant.parse("2026-04-20T20:00:00Z"));
        sessao.setEstado(null);

        when(sessaoRepository.findBySalaIdAndDataHoraBetween(any(), any(), any())).thenReturn(List.of());
        when(sessaoRepository.save(any(Sessao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sessao resultado = sessaoService.criarSessao(sessao);

        assertEquals(EstadoSessao.Aberta, resultado.getEstado());
    }

    @Test
    void eliminarSessaoDeveFalharQuandoExistemBilhetesAssociados() {
        when(sessaoRepository.existsById(10)).thenReturn(true);
        when(bilheteRepository.existsBySessaoId(10)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> sessaoService.eliminarSessao(10));

        assertEquals("Nao e possivel apagar a sessao porque existem bilhetes associados.", exception.getMessage());
        verify(sessaoRepository, never()).deleteById(10);
    }

    private Sessao criarSessao(Integer id, Instant dataHora) {
        return Sessao.builder()
                .id(id)
                .dataHora(dataHora)
                .precoBase(new BigDecimal("20.00"))
                .estado(EstadoSessao.Aberta)
                .evento(Evento.builder().id(1).build())
                .sala(Sala.builder().id(1).build())
                .build();
    }
}
