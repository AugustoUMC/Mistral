package com.tcc.sistema_producao.entidade;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "alocacao_funcionario_grupo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlocacaoFuncionarioGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_producao_id", nullable = false)
    private GrupoProducao grupoProducao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;


    @Column(name = "data_fim")
    private LocalDate dataFim;
}