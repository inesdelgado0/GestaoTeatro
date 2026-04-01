package com.teatro.repositories;

import com.teatro.entities.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {
    List<Pagamento> findByDataPagamentoBetween(Instant inicio, Instant fim);
    List<Pagamento> findByMetodoPagamento(String metodoPagamento);
}
