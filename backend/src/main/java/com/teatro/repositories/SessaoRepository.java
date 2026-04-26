package com.teatro.repositories;

import com.teatro.entities.Sessao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessaoRepository extends JpaRepository<Sessao, Integer> {

    List<Sessao> findByEventoId(Integer eventoId);

    List<Sessao> findByEventoIdOrderByDataHoraAsc(Integer eventoId);

    List<Sessao> findBySalaIdAndDataHoraBetween(Integer salaId, Instant inicio, Instant fim);

    List<Sessao> findBySalaIdOrderByDataHoraAsc(Integer salaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Sessao> findWithLockById(Integer id);

    boolean existsByEventoId(Integer eventoId);

    boolean existsBySalaId(Integer salaId);
}
