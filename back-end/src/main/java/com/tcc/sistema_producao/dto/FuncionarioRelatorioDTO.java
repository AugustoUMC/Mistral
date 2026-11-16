package com.tcc.sistema_producao.dto;

import java.math.BigDecimal;
import java.util.List;

public record FuncionarioRelatorioDTO(
        Long id,
        String nome,
        String matricula,
        String cargo,
        BigDecimal salario,
        Boolean ativo,
        Boolean acessoSistema,
        String perfil,
        List<String> gruposAtuais
) {
}