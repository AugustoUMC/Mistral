package com.tcc.sistema_producao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record AtualizarGruposFuncionarioDTO(

        @NotNull(message = "A lista de grupos é obrigatória")
        List<@NotNull @Positive Long> grupoIds

) {
}