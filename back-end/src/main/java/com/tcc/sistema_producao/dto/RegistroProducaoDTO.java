
package com.tcc.sistema_producao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record RegistroProducaoDTO(

        @NotNull(message = "O grupo de produção é obrigatório")
        @Positive(message = "O ID do grupo deve ser positivo")
        Long grupoProducaoId,

        @NotNull(message = "O produto é obrigatório")
        @Positive(message = "O ID do produto deve ser positivo")
        Long produtoId,

        @NotNull(message = "A data da produção é obrigatória")
        LocalDate dataProducao,

        @NotNull(message = "A quantidade produzida é obrigatória")
        @Min(value = 1, message = "A quantidade produzida deve ser maior que zero")
        Integer quantidadeProduzida,

        @NotNull(message = "A quantidade defeituosa é obrigatória")
        @Min(value = 0, message = "A quantidade defeituosa não pode ser negativa")
        Integer quantidadeDefeituosa

) {
}
