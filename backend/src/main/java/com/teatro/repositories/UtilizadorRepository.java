package com.teatro.repositories;

import com.teatro.entities.Utilizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilizadorRepository extends JpaRepository<Utilizador, Integer> {
    Optional<Utilizador> findByEmail(String email);
    boolean existsByEmail(String email);
}
