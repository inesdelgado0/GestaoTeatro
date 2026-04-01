package com.teatro.controllers;

import com.teatro.dto.BilheteRequestDto;
import com.teatro.dto.BilheteResponseDto;
import com.teatro.dto.LugarBilheteRequestDto;
import com.teatro.dto.LugarBilheteResponseDto;
import com.teatro.entities.Bilhete;
import com.teatro.entities.EstadoBilhete;
import com.teatro.entities.Lugar;
import com.teatro.entities.LugarBilhete;
import com.teatro.entities.Pagamento;
import com.teatro.entities.Sessao;
import com.teatro.entities.Tipobilhete;
import com.teatro.entities.Utilizador;
import com.teatro.services.BilheteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bilhetes")
@RequiredArgsConstructor
public class BilheteController {

    private final BilheteService bilheteService;

    @GetMapping
    public List<BilheteResponseDto> getAll() {
        return bilheteService.listarTodos().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BilheteResponseDto> getById(@PathVariable Integer id) {
        return bilheteService.procurarPorId(id)
                .map(this::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sessao/{sessaoId}")
    public List<BilheteResponseDto> getBySessao(@PathVariable Integer sessaoId) {
        return bilheteService.listarPorSessao(sessaoId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping("/utilizador/{utilizadorId}")
    public List<BilheteResponseDto> getByUtilizador(@PathVariable Integer utilizadorId) {
        return bilheteService.listarPorUtilizador(utilizadorId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<BilheteResponseDto> create(@RequestBody BilheteRequestDto request) {
        if (request.lugarBilhetes() == null || request.lugarBilhetes().isEmpty()) {
            throw new RuntimeException("A compra tem de incluir pelo menos um lugar.");
        }

        Bilhete bilhete = Bilhete.builder()
                .sessao(Sessao.builder().id(request.sessaoId()).build())
                .utilizador(Utilizador.builder().id(request.utilizadorId()).build())
                .pagamento(request.pagamentoId() != null ? Pagamento.builder().id(request.pagamentoId()).build() : null)
                .estado(EstadoBilhete.Reservado)
                .build();

        List<LugarBilhete> lugarBilhetes = request.lugarBilhetes().stream()
                .map(this::toEntity)
                .toList();

        bilhete.setLugarBilhetes(lugarBilhetes);

        Bilhete guardado = bilheteService.criarBilhete(bilhete);
        return ResponseEntity.ok(toResponseDto(guardado));
    }

    private LugarBilhete toEntity(LugarBilheteRequestDto request) {
        return LugarBilhete.builder()
                .lugar(Lugar.builder().id(request.lugarId()).build())
                .tipoBilhete(Tipobilhete.builder().id(request.tipoBilheteId()).build())
                .precoUnitario(request.precoUnitario())
                .build();
    }

    private BilheteResponseDto toResponseDto(Bilhete bilhete) {
        List<LugarBilheteResponseDto> lugarBilhetes = bilhete.getLugarBilhetes().stream()
                .map(lb -> new LugarBilheteResponseDto(
                        lb.getId(),
                        lb.getLugar() != null ? lb.getLugar().getId() : null,
                        lb.getTipoBilhete() != null ? lb.getTipoBilhete().getId() : null,
                        lb.getPrecoUnitario()
                ))
                .toList();

        return new BilheteResponseDto(
                bilhete.getId(),
                bilhete.getSessao() != null ? bilhete.getSessao().getId() : null,
                bilhete.getUtilizador() != null ? bilhete.getUtilizador().getId() : null,
                bilhete.getPagamento() != null ? bilhete.getPagamento().getId() : null,
                bilhete.getEstado() != null ? bilhete.getEstado().toString() : null,
                bilhete.getPrecoFinal(),
                lugarBilhetes
        );
    }
}
