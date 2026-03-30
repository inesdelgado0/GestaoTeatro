package com.teatro.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "tipobilhete")
public class Tipobilhete {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @NotNull
    @Column(name = "percentagemdesconto", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentagemDesconto;

    @OneToMany(mappedBy = "tipoBilhete")
    @Builder.Default
    private List<LugarBilhete> lugarBilhetes = new ArrayList<>();


}