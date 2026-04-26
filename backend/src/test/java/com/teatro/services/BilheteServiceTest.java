package com.teatro.services;

import com.teatro.entities.Bilhete;
import com.teatro.entities.Lugar;
import com.teatro.entities.LugarBilhete;
import com.teatro.entities.Sessao;
import com.teatro.entities.Tipobilhete;
import com.teatro.entities.Utilizador;
import com.teatro.repositories.BilheteRepository;
import com.teatro.repositories.LugarBilheteRepository;
import com.teatro.repositories.SessaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BilheteServiceTest {

    @Mock
    private BilheteRepository bilheteRepository;

    @Mock
    private LugarBilheteRepository lugarBilheteRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @InjectMocks
    private BilheteService bilheteService;

    @Test
    void criarBilheteDeveFalharQuandoOMesmoLugarESelecionadoDuasVezes() {
        Bilhete bilhete = criarBilheteComLugares(11, 7, 7);
        when(sessaoRepository.findWithLockById(11)).thenReturn(Optional.of(Sessao.builder().id(11).build()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bilheteService.criarBilhete(bilhete));

        assertEquals("O mesmo lugar não pode ser selecionado mais do que uma vez na mesma compra.", exception.getMessage());
        verify(bilheteRepository, never()).save(any(Bilhete.class));
    }

    @Test
    void criarBilheteDeveFalharQuandoLugarJaEstaOcupadoNaSessao() {
        Bilhete bilhete = criarBilheteComLugares(11, 7);
        when(sessaoRepository.findWithLockById(11)).thenReturn(Optional.of(Sessao.builder().id(11).build()));
        when(lugarBilheteRepository.existsByLugarIdAndSessaoId(7, 11)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bilheteService.criarBilhete(bilhete));

        assertEquals("Lugar já ocupado para esta sessão.", exception.getMessage());
        verify(bilheteRepository, never()).save(any(Bilhete.class));
    }

    private Bilhete criarBilheteComLugares(Integer sessaoId, Integer... lugarIds) {
        Bilhete bilhete = Bilhete.builder()
                .sessao(Sessao.builder().id(sessaoId).build())
                .utilizador(Utilizador.builder().id(3).build())
                .build();

        List<LugarBilhete> lugares = java.util.Arrays.stream(lugarIds)
                .map(lugarId -> LugarBilhete.builder()
                        .lugar(Lugar.builder().id(lugarId).build())
                        .tipoBilhete(Tipobilhete.builder().id(2).build())
                        .precoUnitario(new BigDecimal("12.50"))
                        .build())
                .toList();

        bilhete.setLugarBilhetes(lugares);
        return bilhete;
    }
}
