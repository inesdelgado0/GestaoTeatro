package com.teatro.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
//@Entity diz ao JPA/Hibernate que esta classe é uma entidade persistente.
//@Table(name = "sessao") diz que esta entidade corresponde à tabela sessao da base de dados.
@Entity
@Table(name = "sessao")
public class Sessao {
    /*
    @Id indica a chave primária.
    @GeneratedValue(...) diz que o id é gerado automaticamente pela BD.
    @Column(name = "id", nullable = false) liga o atributo id à coluna id.
    nullable = false significa: esta coluna não pode ficar a NULL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /*
    dataHora é a data e hora da sessão.
    @NotNull valida em Java que o campo não pode vir vazio.
    @Column(name = "datahora", nullable = false) mapeia para a coluna datahora e diz que na BD também não pode ser nulo.
    Porque usar os dois?
    @NotNull, valida ao nível da aplicação
    nullable = false, reforça ao nível da base de dados
    Ou seja:
    um protege no backend
    o outro protege no schema/SQL
     */
    @NotNull
    @Column(name = "datahora", nullable = false)
    private Instant dataHora;

    /*
    até 10 dígitos no total,2 casas decimais
     */
    @NotNull
    @Column(name = "precobase", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoBase;

    //Porque nullable = false aqui? Porque uma sessão deve ter sempre estado definido. Não faz sentido existir sessão “sem estado”.
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSessao estado;

    //@JoinColumn(name = "id_evento") Diz que a FK na tabela sessao chama-se id_evento.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento", nullable=false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sala", nullable=false)
    private Sala sala;

    @OneToMany(mappedBy = "sessao")
    @Builder.Default
    private List<Bilhete> bilhetes = new ArrayList<>();


}
