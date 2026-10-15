package com.tcc.sistema_producao.entidade;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "meta_producao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetaProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_producao_id", nullable = false)
    private GrupoProducao grupoProducao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Min(value = 1, message = "A quantidade da meta deve ser maior que zero")
    @Column(name = "quantidade_meta", nullable = false)
    private Integer quantidadeMeta;
}