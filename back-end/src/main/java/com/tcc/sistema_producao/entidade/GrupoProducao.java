package com.tcc.sistema_producao.entidade;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "grupo_producao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GrupoProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do grupo é obrigatório")
    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    private Boolean ativo = true;
}