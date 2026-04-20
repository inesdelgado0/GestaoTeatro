package com.teatro.repositories;

import com.teatro.entities.Tipoutilizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoUtilizadorRepository extends JpaRepository<Tipoutilizador, Integer> {
    Optional<Tipoutilizador> findByTipoIgnoreCase(String tipo);
}
