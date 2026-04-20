package com.teatro.services;

import com.teatro.entities.EstadoSessao;
import com.teatro.entities.Sessao;
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

        if (sessao.getEstado() == null) {
            sessao.setEstado(EstadoSessao.Aberta);
        }

        Instant dataHora = sessao.getDataHora();
        List<Sessao> sessoesConflitantes = sessaoRepository.findBySalaIdAndDataHoraBetween(
                sessao.getSala().getId(),
                dataHora.minusSeconds(1),
                dataHora.plusSeconds(1)
        );

        if (!sessoesConflitantes.isEmpty()) {
            throw new RuntimeException("Ja existe uma sessao agendada para essa sala nesse horario.");
        }

        return sessaoRepository.save(sessao);
    }
}
