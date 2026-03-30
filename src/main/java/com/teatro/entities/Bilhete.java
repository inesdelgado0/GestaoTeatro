package com.teatro.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bilhete")
public class Bilhete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "precofinal", precision = 10, scale = 2)
    private BigDecimal precoFinal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoBilhete estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilizador", nullable = false)
    private Utilizador utilizador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sessao", nullable = false)
    private Sessao sessao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pagamento")
    private Pagamento pagamento;

    @OneToMany(mappedBy = "bilhete", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LugarBilhete> lugarBilhetes = new ArrayList<>();


}
