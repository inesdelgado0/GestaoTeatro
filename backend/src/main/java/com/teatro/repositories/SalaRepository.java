package com.teatro.repositories;

import com.teatro.entities.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Integer> {
    Optional<Sala> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}
