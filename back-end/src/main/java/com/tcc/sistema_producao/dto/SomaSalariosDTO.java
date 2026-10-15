package com.tcc.sistema_producao.dto;

import java.math.BigDecimal;

public record SomaSalariosDTO(
        long quantidadeFuncionarios,
        BigDecimal somaSalarios
) {
}