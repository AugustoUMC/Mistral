package com.tcc.sistema_producao.entidade;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String matricula;

    @Column(nullable = false)
    private String operacao;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(name = "data_hora", nullable = false)
    private OffsetDateTime dataHora;
}