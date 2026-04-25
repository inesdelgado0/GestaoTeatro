package com.teatro.services;

import com.teatro.dto.RelatorioOcupacaoDto;
import com.teatro.dto.RelatorioOcupacaoItemDto;
import com.teatro.dto.RelatorioVendasDto;
import com.teatro.dto.RelatorioVendasItemDto;
import com.teatro.entities.Bilhete;
import com.teatro.entities.EstadoBilhete;
import com.teatro.entities.Sessao;
import com.teatro.repositories.BilheteRepository;
import com.teatro.repositories.SessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final BilheteRepository bilheteRepository;
    private final SessaoRepository sessaoRepository;

    @Transactional(readOnly = true)
    public RelatorioVendasDto gerarRelatorioVendas(Instant inicio, Instant fim) {
        validarPeriodo(inicio, fim);

        List<Bilhete> bilhetes = bilheteRepository.findAll().stream()
                .filter(this::isBilhetePago)
                .filter(bilhete -> bilhete.getPagamento() != null && bilhete.getPagamento().getDataPagamento() != null)
                .filter(bilhete -> isBetweenInclusive(bilhete.getPagamento().getDataPagamento(), inicio, fim))
                .toList();

        List<RelatorioVendasItemDto> itens = bilhetes.stream()
                .collect(java.util.stream.Collectors.groupingBy(Bilhete::getSessao))
                .entrySet().stream()
                .map(entry -> {
                    Sessao sessao = entry.getKey();
                    List<Bilhete> bilhetesSessao = entry.getValue();
                    long totalBilhetes = bilhetesSessao.size();
                    BigDecimal totalFaturado = bilhetesSessao.stream()
                            .map(Bilhete::getPrecoFinal)
                            .filter(java.util.Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new RelatorioVendasItemDto(
                            sessao.getId(),
                            sessao.getEvento() != null ? sessao.getEvento().getTitulo() : "",
                            sessao.getSala() != null ? sessao.getSala().getNome() : "",
                            sessao.getDataHora(),
                            totalBilhetes,
                            totalFaturado
                    );
                })
                .sorted(Comparator.comparing(RelatorioVendasItemDto::dataHoraSessao, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        long totalBilhetes = itens.stream()
                .mapToLong(RelatorioVendasItemDto::totalBilhetes)
                .sum();

        BigDecimal totalFaturado = itens.stream()
                .map(RelatorioVendasItemDto::totalFaturado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RelatorioVendasDto(inicio, fim, totalBilhetes, totalFaturado, itens);
    }

    @Transactional(readOnly = true)
    public RelatorioOcupacaoDto gerarRelatorioOcupacao(Instant inicio, Instant fim) {
        validarPeriodo(inicio, fim);

        List<RelatorioOcupacaoItemDto> itens = sessaoRepository.findAll().stream()
                .filter(sessao -> sessao.getDataHora() != null && isBetweenInclusive(sessao.getDataHora(), inicio, fim))
                .map(sessao -> {
                    int capacidadeSala = sessao.getSala() != null && sessao.getSala().getCapacidadeTotal() != null
                            ? sessao.getSala().getCapacidadeTotal()
                            : 0;

                    long lugaresOcupados = sessao.getBilhetes().stream()
                            .filter(this::isBilheteAtivoParaOcupacao)
                            .flatMap(bilhete -> bilhete.getLugarBilhetes().stream())
                            .count();

                    BigDecimal taxaOcupacao = capacidadeSala <= 0
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(lugaresOcupados)
                            .multiply(new BigDecimal("100"))
                            .divide(BigDecimal.valueOf(capacidadeSala), 2, RoundingMode.HALF_UP);

                    return new RelatorioOcupacaoItemDto(
                            sessao.getId(),
                            sessao.getEvento() != null ? sessao.getEvento().getTitulo() : "",
                            sessao.getSala() != null ? sessao.getSala().getNome() : "",
                            sessao.getDataHora(),
                            capacidadeSala,
                            lugaresOcupados,
                            taxaOcupacao
                    );
                })
                .sorted(Comparator.comparing(RelatorioOcupacaoItemDto::dataHoraSessao, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        BigDecimal taxaMedia = itens.isEmpty()
                ? BigDecimal.ZERO
                : itens.stream()
                .map(RelatorioOcupacaoItemDto::taxaOcupacao)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(itens.size()), 2, RoundingMode.HALF_UP);

        return new RelatorioOcupacaoDto(inicio, fim, taxaMedia, itens);
    }

    private void validarPeriodo(Instant inicio, Instant fim) {
        if (inicio == null || fim == null) {
            throw new RuntimeException("O periodo do relatorio e obrigatorio.");
        }

        if (fim.isBefore(inicio)) {
            throw new RuntimeException("A data final nao pode ser anterior a data inicial.");
        }
    }

    private boolean isBetweenInclusive(Instant valor, Instant inicio, Instant fim) {
        return !valor.isBefore(inicio) && !valor.isAfter(fim);
    }

    private boolean isBilhetePago(Bilhete bilhete) {
        return bilhete.getEstado() == EstadoBilhete.Pago;
    }

    private boolean isBilheteAtivoParaOcupacao(Bilhete bilhete) {
        return bilhete.getEstado() == EstadoBilhete.Pago || bilhete.getEstado() == EstadoBilhete.Reservado;
    }
}
