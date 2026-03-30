package com.teatro.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tipoutilizador")
public class Tipoutilizador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @OneToMany(mappedBy = "tipoUtilizador")
    @Builder.Default
    private List<Utilizador> utilizadores = new ArrayList<>();


}