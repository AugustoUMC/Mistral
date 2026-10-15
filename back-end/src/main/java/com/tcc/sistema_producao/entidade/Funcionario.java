
package com.tcc.sistema_producao.entidade;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "funcionario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "A matrícula é obrigatória")
    @Column(nullable = false, unique = true)
    private String matricula;

    @NotBlank(message = "O cargo é obrigatório")
    @Column(nullable = false)
    private String cargo;

    @NotNull(message = "O salário é obrigatório")
    @DecimalMin(value = "0.00", message = "O salário não pode ser negativo")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salario = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false)
    private Boolean acessoSistema = false;

    @Column
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @Column(nullable = false)
    private String perfil = "USUARIO";
}
