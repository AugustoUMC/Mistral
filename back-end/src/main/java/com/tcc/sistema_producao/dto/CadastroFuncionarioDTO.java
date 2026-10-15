
package com.tcc.sistema_producao.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CadastroFuncionarioDTO(

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "A matrícula é obrigatória")
        String matricula,

        @NotBlank(message = "O cargo é obrigatório")
        String cargo,

        @NotNull(message = "O salário é obrigatório")
        @DecimalMin(
                value = "0.00",
                message = "O salário não pode ser negativo"
        )
        BigDecimal salario,

        String senha,

        Boolean acessoSistema

) {
}
