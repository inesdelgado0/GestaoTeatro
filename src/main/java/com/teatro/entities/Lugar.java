package com.teatro.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lugar")
public class Lugar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 5)
    @NotNull
    @Column(name = "fila", nullable = false, length = 5)
    private String fila;

    @NotNull
    @Column(name = "numero", nullable = false)
    private Integer numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_zona", nullable = false)
    private Zona zona;

    @OneToMany(mappedBy = "lugar")
    @Builder.Default
    private List<LugarBilhete> lugarBilhetes = new ArrayList<>();


}
