package com.teatro.services;

import com.teatro.entities.EstadoSessao;
import com.teatro.entities.Evento;
import com.teatro.entities.Sala;
import com.teatro.entities.Sessao;
import com.teatro.repositories.BilheteRepository;
import com.teatro.repositories.EventoRepository;
import com.teatro.repositories.SalaRepository;
import com.teatro.repositories.SessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessaoService {

    private final SessaoRepository sessaoRepository;
    private final BilheteRepository bilheteRepository;
    private final EventoRepository eventoRepository;
    private final SalaRepository salaRepository;

    public List<Sessao> listarTodas() {
        return sessaoRepository.findAll();
    }

    public Optional<Sessao> procurarPorId(Integer id) {
        return sessaoRepository.findById(id);
    }

    public List<Sessao> listarPorEvento(Integer eventoId) {
        return sessaoRepository.findByEventoIdOrderByDataHoraAsc(eventoId);
    }

    public Sessao criarSessao(Sessao sessao) {
        validarSessao(sessao);

        if (sessao.getEstado() == null) {
            sessao.setEstado(EstadoSessao.Aberta);
        }

        validarConflitoDeHorario(sessao, null);
        return sessaoRepository.save(sessao);
    }

    public Sessao atualizarSessao(Integer id, Sessao sessaoAtualizada) {
        Sessao sessaoExistente = sessaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sessão não encontrada."));

        validarSessao(sessaoAtualizada);

        if (sessaoAtualizada.getEstado() == null) {
            sessaoAtualizada.setEstado(
                    sessaoExistente.getEstado() != null ? sessaoExistente.getEstado() : EstadoSessao.Aberta
            );
        }

        validarConflitoDeHorario(sessaoAtualizada, id);

        sessaoExistente.setDataHora(sessaoAtualizada.getDataHora());
        sessaoExistente.setPrecoBase(sessaoAtualizada.getPrecoBase());
        sessaoExistente.setEstado(sessaoAtualizada.getEstado());
        sessaoExistente.setEvento(sessaoAtualizada.getEvento());
        sessaoExistente.setSala(sessaoAtualizada.getSala());

        return sessaoRepository.save(sessaoExistente);
    }

    public void eliminarSessao(Integer id) {
        if (!sessaoRepository.existsById(id)) {
            throw new RuntimeException("Não é possível apagar: sessão não encontrada.");
        }

        if (bilheteRepository.existsBySessaoId(id)) {
            throw new RuntimeException("Não é possível apagar a sessão porque existem bilhetes associados.");
        }

        sessaoRepository.deleteById(id);
    }

    private void validarSessao(Sessao sessao) {
        if (sessao.getEvento() == null) {
            throw new RuntimeException("A sessão tem de estar associada a um evento.");
        }

        if (sessao.getEvento().getId() == null) {
            throw new RuntimeException("A sessão tem de estar associada a um evento válido.");
        }

        if (sessao.getSala() == null) {
            throw new RuntimeException("A sessão tem de estar associada a uma sala.");
        }

        if (sessao.getSala().getId() == null) {
            throw new RuntimeException("A sessão tem de estar associada a uma sala válida.");
        }

        if (sessao.getDataHora() == null) {
            throw new RuntimeException("A sessão tem de ter data e hora definidas.");
        }

        if (sessao.getPrecoBase() == null) {
            throw new RuntimeException("A sessão tem de ter preço base definido.");
        }

        if (sessao.getPrecoBase().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("O preço base da sessão não pode ser negativo.");
        }

        Evento evento = eventoRepository.findById(sessao.getEvento().getId())
                .orElseThrow(() -> new RuntimeException("O evento associado à sessão não existe."));

        Sala sala = salaRepository.findById(sessao.getSala().getId())
                .orElseThrow(() -> new RuntimeException("A sala associada à sessão não existe."));

        sessao.setEvento(evento);
        sessao.setSala(sala);
    }

    private void validarConflitoDeHorario(Sessao sessao, Integer sessaoIdIgnorada) {
        Instant novaSessaoInicio = sessao.getDataHora();
        Instant novaSessaoFim = calcularFimSessao(sessao);

        boolean existeConflito = sessaoRepository.findBySalaIdOrderByDataHoraAsc(sessao.getSala().getId()).stream()
                .filter(sessaoExistente -> sessaoIdIgnorada == null || !sessaoIdIgnorada.equals(sessaoExistente.getId()))
                .filter(sessaoExistente -> sessaoExistente.getDataHora() != null)
                .anyMatch(sessaoExistente -> {
                    Instant sessaoExistenteInicio = sessaoExistente.getDataHora();
                    Instant sessaoExistenteFim = calcularFimSessao(sessaoExistente);
                    return novaSessaoInicio.isBefore(sessaoExistenteFim)
                            && sessaoExistenteInicio.isBefore(novaSessaoFim);
                });

        if (existeConflito) {
            throw new RuntimeException("Já existe uma sessão agendada para essa sala nesse horário.");
        }
    }

    private Instant calcularFimSessao(Sessao sessao) {
        int duracaoMinutos = 1;
        if (sessao.getEvento() != null && sessao.getEvento().getDuracaoMin() != null) {
            duracaoMinutos = Math.max(1, sessao.getEvento().getDuracaoMin());
        }
        return sessao.getDataHora().plus(Duration.ofMinutes(duracaoMinutos));
    }
}
