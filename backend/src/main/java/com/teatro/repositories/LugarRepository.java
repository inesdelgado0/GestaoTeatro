package com.teatro.repositories;

import com.teatro.entities.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, Integer> {
    List<Lugar> findByZonaId(Integer zonaId);
    List<Lugar> findByZonaIdOrderByFilaAscNumeroAsc(Integer zonaId);
    Optional<Lugar> findByZonaIdAndFilaAndNumero(Integer zonaId, String fila, Integer numero);
    Optional<Lugar> findByZonaSalaIdAndFilaAndNumero(Integer salaId, String fila, Integer numero);
    boolean existsByZonaId(Integer zonaId);

}

//obter todos os lugares de uma zona
//obter os lugares de uma zona ja ordenados, o que é util para mostrar mapas de lugares
// para encontrar um lugar especifico e validar se ja existe quando tiver a configurar a sala
