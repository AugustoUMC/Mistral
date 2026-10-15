
package com.tcc.sistema_producao.entidade;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "registro_producao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O grupo de produção é obrigatório")
    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_producao_id", nullable = false)
    private GrupoProducao grupoProducao;

    @NotNull(message = "O produto é obrigatório")
    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @NotNull(message = "A data da produção é obrigatória")
    @Column(name = "data_producao", nullable = false)
    private LocalDate dataProducao;

    @NotNull(message = "A quantidade produzida é obrigatória")
    @Min(value = 1, message = "A quantidade produzida deve ser maior que zero")
    @Column(name = "quantidade_produzida", nullable = false)
    private Integer quantidadeProduzida;

    @NotNull(message = "A quantidade de peças defeituosas é obrigatória")
    @Min(value = 0, message = "A quantidade de peças defeituosas não pode ser negativa")
    @Column(name = "quantidade_defeituosa", nullable = false)
    private Integer quantidadeDefeituosa = 0;
}
