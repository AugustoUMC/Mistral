package com.tcc.sistema_producao.dto;

import java.math.BigDecimal;

public record FuncionarioRelatorioDTO(
        Long id,
        String nome,
        String matricula,
        String cargo,
        BigDecimal salario,
        Boolean ativo,
        Boolean acessoSistema,
        String perfil
) {
}