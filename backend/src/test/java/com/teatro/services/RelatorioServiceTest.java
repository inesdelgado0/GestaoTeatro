package com.teatro.services;

import com.teatro.dto.RelatorioOcupacaoDto;
import com.teatro.dto.RelatorioVendasDto;
import com.teatro.entities.Bilhete;
import com.teatro.entities.EstadoBilhete;
import com.teatro.entities.Evento;
import com.teatro.entities.Pagamento;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private BilheteRepository bilheteRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    void gerarRelatorioVendasDeveSomarApenasBilhetesPagosNoPeriodo() {
        Instant inicio = Instant.parse("2026-04-01T00:00:00Z");
        Instant fim = Instant.parse("2026-04-30T23:59:59Z");

        Sessao sessao = criarSessao(10, "Hamlet", "Sala Principal", 100, Instant.parse("2026-04-15T21:00:00Z"));
        Bilhete pagoNoPeriodo = criarBilhete(sessao, EstadoBilhete.Pago, "25.00", Instant.parse("2026-04-10T10:00:00Z"));
        Bilhete reservado = criarBilhete(sessao, EstadoBilhete.Reservado, "25.00", Instant.parse("2026-04-11T10:00:00Z"));
        Bilhete pagoForaPeriodo = criarBilhete(sessao, EstadoBilhete.Pago, "30.00", Instant.parse("2026-05-01T10:00:00Z"));

        when(bilheteRepository.findAll()).thenReturn(List.of(pagoNoPeriodo, reservado, pagoForaPeriodo));

        RelatorioVendasDto relatorio = relatorioService.gerarRelatorioVendas(inicio, fim);

        assertEquals(1, relatorio.totalBilhetes());
        assertEquals(new BigDecimal("25.00"), relatorio.totalFaturado());
        assertEquals(1, relatorio.itens().size());
        assertEquals("Hamlet", relatorio.itens().getFirst().eventoTitulo());
    }

    @Test
    void gerarRelatorioOcupacaoDeveCalcularTaxaPorSessaoEMedia() {
        Instant inicio = Instant.parse("2026-04-01T00:00:00Z");
        Instant fim = Instant.parse("2026-04-30T23:59:59Z");

        Sessao sessaoA = criarSessao(1, "Hamlet", "Sala A", 100, Instant.parse("2026-04-05T20:00:00Z"));
        Sessao sessaoB = criarSessao(2, "Macbeth", "Sala B", 50, Instant.parse("2026-04-06T20:00:00Z"));

        sessaoA.setBilhetes(List.of(
                criarBilhete(sessaoA, EstadoBilhete.Pago, "20.00", Instant.parse("2026-04-01T10:00:00Z"), 2),
                criarBilhete(sessaoA, EstadoBilhete.Reservado, "20.00", Instant.parse("2026-04-01T11:00:00Z"), 1)
        ));
        sessaoB.setBilhetes(List.of(
                criarBilhete(sessaoB, EstadoBilhete.Pago, "15.00", Instant.parse("2026-04-02T10:00:00Z"), 10)
        ));

        when(sessaoRepository.findAll()).thenReturn(List.of(sessaoA, sessaoB));

        RelatorioOcupacaoDto relatorio = relatorioService.gerarRelatorioOcupacao(inicio, fim);

        assertEquals(2, relatorio.itens().size());
        assertEquals(new BigDecimal("3.00"), relatorio.itens().get(0).taxaOcupacao());
        assertEquals(new BigDecimal("20.00"), relatorio.itens().get(1).taxaOcupacao());
        assertEquals(new BigDecimal("11.50"), relatorio.taxaMediaOcupacao());
    }

    private Sessao criarSessao(Integer id, String tituloEvento, String nomeSala, Integer capacidade, Instant dataHora) {
        Evento evento = Evento.builder().id(id).titulo(tituloEvento).build();
        Sala sala = Sala.builder().id(id).nome(nomeSala).capacidadeTotal(capacidade).build();
        return Sessao.builder()
                .id(id)
                .evento(evento)
                .sala(sala)
                .dataHora(dataHora)
                .build();
    }

    private Bilhete criarBilhete(Sessao sessao, EstadoBilhete estado, String precoFinal, Instant dataPagamento) {
        return criarBilhete(sessao, estado, precoFinal, dataPagamento, 1);
    }

    private Bilhete criarBilhete(Sessao sessao, EstadoBilhete estado, String precoFinal, Instant dataPagamento, int lugares) {
        Pagamento pagamento = Pagamento.builder().dataPagamento(dataPagamento).build();
        return Bilhete.builder()
                .sessao(sessao)
                .estado(estado)
                .precoFinal(new BigDecimal(precoFinal))
                .pagamento(pagamento)
                .lugarBilhetes(java.util.stream.IntStream.range(0, lugares)
                        .mapToObj(index -> com.teatro.entities.LugarBilhete.builder().build())
                        .toList())
                .build();
    }
}
