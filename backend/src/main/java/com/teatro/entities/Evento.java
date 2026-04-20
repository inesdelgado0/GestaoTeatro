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
@Table(name = "evento")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 200)
    @NotNull
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "duracaomin")
    private Integer duracaoMin;

    @Size(max = 20)
    @Column(name = "classificacaoetaria", length = 20)
    private String classificacaoEtaria;

    @Size(max = 50)
    @Column(name = "genero", length = 50)
    private String genero;

    @OneToMany(mappedBy = "evento")
    @Builder.Default
    private List<Sessao> sessoes = new ArrayList<>();

}