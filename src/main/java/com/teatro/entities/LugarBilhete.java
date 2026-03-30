package com.teatro.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lugar_bilhete")
public class LugarBilhete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "precounitario", precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bilhete", nullable = false)
    private Bilhete bilhete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lugar", nullable = false)
    private Lugar lugar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_bilhete", nullable=false)
    private Tipobilhete tipoBilhete;


}