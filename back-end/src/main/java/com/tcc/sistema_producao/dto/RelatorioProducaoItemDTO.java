
package com.tcc.sistema_producao.dto;

import java.math.BigDecimal;

public record RelatorioProducaoItemDTO(
        Long grupoProducaoId,
        String grupoProducaoNome,
        Long produtoId,
        String produtoCodigo,
        String produtoDescricao,
        long quantidadeMeta,
        long quantidadeProduzida,
        long quantidadeDefeituosa,
        BigDecimal percentualMetaAtingida,
        BigDecimal percentualDefeituoso
) {
}
