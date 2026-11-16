package com.tcc.sistema_producao.entidade;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "apontamento_jornada")
@Getter
@Setter
@NoArgsConstructor
public class ApontamentoJornada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O funcionário é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @NotNull(message = "O grupo de produção é obrigatório.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_producao_id", nullable = false)
    private GrupoProducao grupoProducao;

    @NotNull(message = "O início do apontamento é obrigatório.")
    @Column(name = "inicio_em", nullable = false)
    private LocalDateTime inicioEm;

    @NotNull(message = "O término do apontamento é obrigatório.")
    @Column(name = "fim_em", nullable = false)
    private LocalDateTime fimEm;

    @NotNull(message = "A duração dos intervalos é obrigatória.")
    @Min(value = 0, message = "A duração dos intervalos não pode ser negativa.")
    @Column(name = "intervalo_minutos", nullable = false)
    private Integer intervaloMinutos = 0;

    @NotNull(message = "As horas líquidas devem ser calculadas pelo sistema.")
    @Min(value = 0, message = "As horas líquidas não podem ser negativas.")
    @Column(name = "minutos_liquidos", nullable = false)
    private Integer minutosLiquidos;
}