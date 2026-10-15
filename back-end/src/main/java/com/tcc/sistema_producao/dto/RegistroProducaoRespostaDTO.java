
package com.tcc.sistema_producao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistroProducaoRespostaDTO(
        Long id,
        Long grupoProducaoId,
        String grupoProducaoNome,
        Long produtoId,
        String produtoCodigo,
        String produtoDescricao,
        LocalDate dataProducao,
        Integer quantidadeProduzida,
        Integer quantidadeDefeituosa,
        BigDecimal percentualDefeituoso
) {
}
