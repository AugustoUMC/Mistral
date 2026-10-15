package com.tcc.sistema_producao.entidade;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O código é obrigatório")
    @Column(nullable = false, unique = true)
    private String codigo;

    @NotBlank(message = "A descrição é obrigatória")
    @Column(nullable = false)
    private String descricao;

    private String ean;

    private String dun;

    @Column(name = "caixa_master")
    private Integer caixaMaster;

    @Column(name = "diametro_cm")
    private BigDecimal diametroCm;

    private BigDecimal altura;

    @Column(name = "preco_bruto")
    private BigDecimal precoBruto;

    private String imagem;
}