package com.teatro.repositories;

import com.teatro.entities.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SessaoRepository extends JpaRepository<Sessao, Integer> {

    List<Sessao> findByEventoId(Integer eventoId);

    List<Sessao> findByEventoIdOrderByDataHoraAsc(Integer eventoId);

    List<Sessao> findBySalaIdAndDataHoraBetween(Integer salaId, Instant inicio, Instant fim);
}

//Procura todas as sessões de um determinado evento.
//Procura sessões de uma determinada sala dentro de um intervalo de tempo.