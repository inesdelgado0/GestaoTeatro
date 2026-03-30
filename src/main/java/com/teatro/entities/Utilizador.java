package com.teatro.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "utilizador")
public class Utilizador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Size(max = 100)
    @NotNull
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Size(max = 20)
    @Column(name = "telemovel", length = 20)
    private String telemovel;

    @Column(name = "morada")
    private String morada;

    @Size(max = 15)
    @Column(name = "nif", length = 15)
    private String nif;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_utilizador", nullable = false)
    private Tipoutilizador tipoUtilizador;


}