package com.tcc.sistema_producao.dto;

import java.time.LocalDate;

public record AlocacaoFuncionarioGrupoDTO(
        Long id,
        Long grupoId,
        String grupoNome,
        LocalDate dataInicio,
        LocalDate dataFim,
        boolean atual
) {
}