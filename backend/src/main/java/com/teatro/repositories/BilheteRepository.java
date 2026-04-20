package com.teatro.repositories;

import com.teatro.entities.Bilhete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BilheteRepository extends JpaRepository<Bilhete, Integer> {
    List<Bilhete> findBySessaoId(Integer sessaoId);
    List<Bilhete> findByUtilizadorId(Integer utilizadorId);
}

//veriffica todas as compras associadas a uma sessao
//verifica todos as compras feitas por um utilizador