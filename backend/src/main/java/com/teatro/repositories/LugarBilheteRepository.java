package com.teatro.repositories;

import com.teatro.entities.LugarBilhete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface LugarBilheteRepository extends JpaRepository<LugarBilhete, Integer> {

    @Query("""
            select count(lb) > 0
            from LugarBilhete lb
            where lb.lugar.id = :lugarId
              and lb.bilhete.sessao.id = :sessaoId
            """)
    boolean existsByLugarIdAndSessaoId(@Param("lugarId") Integer lugarId, @Param("sessaoId") Integer sessaoId);

    boolean existsByLugarId(Integer lugarId);
    boolean existsByTipoBilheteId(Integer tipoBilheteId);
}

// Verifica se um determinado lugar já está associado a um bilhete
// da mesma sessão, impedindo vendas duplicadas.
