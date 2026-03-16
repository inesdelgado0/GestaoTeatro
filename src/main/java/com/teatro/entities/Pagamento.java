package com.teatro.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

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
    private BigDecimal valortotal;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "datapagamento")
    private Instant datapagamento;

    @Size(max = 50)
    @Column(name = "metodopagamento", length = 50)
    private String metodopagamento;


}