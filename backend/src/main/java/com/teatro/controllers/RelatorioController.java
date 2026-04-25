package com.teatro.controllers;

import com.teatro.dto.RelatorioOcupacaoDto;
import com.teatro.dto.RelatorioVendasDto;
import com.teatro.services.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/vendas")
    public RelatorioVendasDto getRelatorioVendas(@RequestParam Instant inicio, @RequestParam Instant fim) {
        return relatorioService.gerarRelatorioVendas(inicio, fim);
    }

    @GetMapping("/ocupacao")
    public RelatorioOcupacaoDto getRelatorioOcupacao(@RequestParam Instant inicio, @RequestParam Instant fim) {
        return relatorioService.gerarRelatorioOcupacao(inicio, fim);
    }
}
