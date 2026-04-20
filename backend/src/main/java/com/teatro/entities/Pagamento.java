package com.teatro.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pagamento")
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "valortotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "datapagamento")
    private Instant dataPagamento;

    @Size(max = 50)
    @Column(name = "metodopagamento", length = 50)
    private String metodoPagamento;

    @OneToMany(mappedBy = "pagamento")
    @Builder.Default
    private List<Bilhete> bilhetes = new ArrayList<>();


}