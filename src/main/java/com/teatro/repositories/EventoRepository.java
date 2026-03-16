package com.teatro.repositories;

import com.teatro.entities.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Integer> {

    Optional<Evento> findByTitulo(String titulo);

    List<Evento> findByGenero(String genero);

    List<Evento> findByTituloContainingIgnoreCase(String termo);

    @Query("SELECT e FROM Evento e WHERE e.duracaomin > :minutos")
    List<Evento> findEventosLongos(@Param("minutos") Integer minutos);
}
