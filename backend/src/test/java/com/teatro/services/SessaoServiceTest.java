package com.teatro.services;

import com.teatro.entities.EstadoSessao;
import com.teatro.entities.Evento;
import com.teatro.entities.Sala;
import com.teatro.entities.Sessao;
import com.teatro.repositories.BilheteRepository;
import com.teatro.repositories.EventoRepository;
import com.teatro.repositories.SalaRepository;
import com.teatro.repositories.SessaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessaoServiceTest {

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private BilheteRepository bilheteRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private SalaRepository salaRepository;

    @InjectMocks
    private SessaoService sessaoService;

    @Test
    void criarSessaoDeveFalharQuandoExisteConflitoDeHorario() {
        Sessao sessao = criarSessao(1, Instant.parse("2026-04-20T20:00:00Z"));
        mockEntidadesAssociadas(sessao);
        when(sessaoRepository.findBySalaIdOrderByDataHoraAsc(any()))
                .thenReturn(List.of(criarSessao(2, Instant.parse("2026-04-20T20:00:00Z"))));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> sessaoService.criarSessao(sessao));

        assertEquals("Já existe uma sessão agendada para essa sala nesse horário.", exception.getMessage());
    }

    @Test
    void criarSessaoDeveAssumirEstadoAbertaQuandoNaoForDefinido() {
        Sessao sessao = criarSessao(1, Instant.parse("2026-04-20T20:00:00Z"));
        sessao.setEstado(null);
        mockEntidadesAssociadas(sessao);

        when(sessaoRepository.findBySalaIdOrderByDataHoraAsc(any())).thenReturn(List.of());
        when(sessaoRepository.save(any(Sessao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sessao resultado = sessaoService.criarSessao(sessao);

        assertEquals(EstadoSessao.Aberta, resultado.getEstado());
    }

    @Test
    void criarSessaoDeveFalharQuandoEventoNaoExiste() {
        Sessao sessao = criarSessao(1, Instant.parse("2026-04-20T20:00:00Z"));
        when(eventoRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> sessaoService.criarSessao(sessao));

        assertEquals("O evento associado à sessão não existe.", exception.getMessage());
        verify(sessaoRepository, never()).save(any(Sessao.class));
    }

    @Test
    void criarSessaoDeveFalharQuandoPrecoBaseENegativo() {
        Sessao sessao = criarSessao(1, Instant.parse("2026-04-20T20:00:00Z"));
        sessao.setPrecoBase(new BigDecimal("-1.00"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> sessaoService.criarSessao(sessao));

        assertEquals("O preço base da sessão não pode ser negativo.", exception.getMessage());
        verify(sessaoRepository, never()).save(any(Sessao.class));
    }

    @Test
    void criarSessaoDeveSubstituirReferenciasPorEntidadesGeridas() {
        Sessao sessao = criarSessao(1, Instant.parse("2026-04-20T20:00:00Z"));
        Evento eventoGerido = Evento.builder().id(1).titulo("Hamlet").build();
        Sala salaGerida = Sala.builder().id(1).nome("Sala A").capacidadeTotal(100).build();

        when(eventoRepository.findById(1)).thenReturn(Optional.of(eventoGerido));
        when(salaRepository.findById(1)).thenReturn(Optional.of(salaGerida));
        when(sessaoRepository.findBySalaIdOrderByDataHoraAsc(eq(1))).thenReturn(List.of());
        when(sessaoRepository.save(any(Sessao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Sessao resultado = sessaoService.criarSessao(sessao);

        assertSame(eventoGerido, resultado.getEvento());
        assertSame(salaGerida, resultado.getSala());
    }

    @Test
    void eliminarSessaoDeveFalharQuandoExistemBilhetesAssociados() {
        when(sessaoRepository.existsById(10)).thenReturn(true);
        when(bilheteRepository.existsBySessaoId(10)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> sessaoService.eliminarSessao(10));

        assertEquals("Não é possível apagar a sessão porque existem bilhetes associados.", exception.getMessage());
        verify(sessaoRepository, never()).deleteById(10);
    }

    @Test
    void criarSessaoDeveFalharQuandoSobrepoeDuracaoDeOutraSessao() {
        Sessao sessaoNova = criarSessao(3, Instant.parse("2026-04-20T22:00:00Z"));
        mockEntidadesAssociadas(sessaoNova);

        Evento eventoLongo = Evento.builder().id(1).duracaoMin(120).build();
        Sala sala = Sala.builder().id(1).nome("Sala A").capacidadeTotal(100).build();
        Sessao sessaoExistente = Sessao.builder()
                .id(2)
                .dataHora(Instant.parse("2026-04-20T21:00:00Z"))
                .precoBase(new BigDecimal("20.00"))
                .estado(EstadoSessao.Aberta)
                .evento(eventoLongo)
                .sala(sala)
                .build();

        when(sessaoRepository.findBySalaIdOrderByDataHoraAsc(1)).thenReturn(List.of(sessaoExistente));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> sessaoService.criarSessao(sessaoNova));

        assertEquals("Já existe uma sessão agendada para essa sala nesse horário.", exception.getMessage());
        verify(sessaoRepository, never()).save(any(Sessao.class));
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

    private void mockEntidadesAssociadas(Sessao sessao) {
        when(eventoRepository.findById(sessao.getEvento().getId()))
                .thenReturn(Optional.of(Evento.builder().id(sessao.getEvento().getId()).build()));
        when(salaRepository.findById(sessao.getSala().getId()))
                .thenReturn(Optional.of(Sala.builder().id(sessao.getSala().getId()).capacidadeTotal(100).build()));
    }
}
