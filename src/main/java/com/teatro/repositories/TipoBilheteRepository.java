package com.teatro.repositories;

import com.teatro.entities.Tipobilhete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoBilheteRepository extends JpaRepository<Tipobilhete, Integer> {
    Optional<Tipobilhete> findByNomeIgnoreCase(String nome);
}
