package com.teatro.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @ColumnDefault("0")
    @Column(name = "precounitario", precision = 10, scale = 2)
    private BigDecimal precounitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_bilhete")
    private Bilhete idBilhete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lugar")
    private Lugar idLugar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_bilhete")
    private Tipobilhete idTipoBilhete;


}