package com.teatro.services;

import com.teatro.entities.EstadoSessao;
import com.teatro.entities.Sessao;
import com.teatro.repositories.BilheteRepository;
import com.teatro.repositories.SessaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessaoService {

    private final SessaoRepository sessaoRepository;
    private final BilheteRepository bilheteRepository;

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
                .orElseThrow(() -> new RuntimeException("Sessao nao encontrada."));

        validarSessao(sessaoAtualizada);

        if (sessaoAtualizada.getEstado() == null) {
            sessaoAtualizada.setEstado(sessaoExistente.getEstado() != null ? sessaoExistente.getEstado() : EstadoSessao.Aberta);
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
            throw new RuntimeException("Nao e possivel apagar: sessao nao encontrada.");
        }

        if (bilheteRepository.existsBySessaoId(id)) {
            throw new RuntimeException("Nao e possivel apagar a sessao porque existem bilhetes associados.");
        }

        sessaoRepository.deleteById(id);
    }

    private void validarSessao(Sessao sessao) {
        if (sessao.getEvento() == null) {
            throw new RuntimeException("A sessao tem de estar associada a um evento.");
        }

        if (sessao.getSala() == null) {
            throw new RuntimeException("A sessao tem de estar associada a uma sala.");
        }

        if (sessao.getDataHora() == null) {
            throw new RuntimeException("A sessao tem de ter data e hora definidas.");
        }

        if (sessao.getPrecoBase() == null) {
            throw new RuntimeException("A sessao tem de ter preco base definido.");
        }
    }

    private void validarConflitoDeHorario(Sessao sessao, Integer sessaoIdIgnorada) {
        Instant dataHora = sessao.getDataHora();
        List<Sessao> sessoesConflitantes = sessaoRepository.findBySalaIdAndDataHoraBetween(
                sessao.getSala().getId(),
                dataHora.minusSeconds(1),
                dataHora.plusSeconds(1)
        );

        if (sessaoIdIgnorada != null) {
            sessoesConflitantes = sessoesConflitantes.stream()
                    .filter(sessaoExistente -> !sessaoIdIgnorada.equals(sessaoExistente.getId()))
                    .toList();
        }

        if (!sessoesConflitantes.isEmpty()) {
            throw new RuntimeException("Ja existe uma sessao agendada para essa sala nesse horario.");
        }
    }
}
