package com.teatro.repositories;

import com.teatro.entities.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZonaRepository extends JpaRepository<Zona, Integer> {
    List<Zona> findBySalaId(Integer salaId);
}
// Procura todas as zonas que pertencem a uma determinada sala
/* Isto porque as salas podem ter diferentes tamanhos e ter zonas diferentes ou mais ou menos zonas*/