package com.tcc.sistema_producao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MetaProducaoDTO(

        @NotNull(message = "O grupo de produção é obrigatório")
        Long grupoProducaoId,

        @NotNull(message = "O produto é obrigatório")
        Long produtoId,

        @NotNull(message = "A quantidade da meta é obrigatória")
        @Min(value = 1, message = "A quantidade da meta deve ser maior que zero")
        Integer quantidadeMeta

) {
}