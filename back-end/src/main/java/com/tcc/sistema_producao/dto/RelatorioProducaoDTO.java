
package com.tcc.sistema_producao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioProducaoDTO(
        LocalDate dataInicial,
        LocalDate dataFinal,
        long quantidadeMetaTotal,
        long quantidadeProduzidaTotal,
        long quantidadeDefeituosaTotal,
        BigDecimal percentualMetaAtingida,
        BigDecimal percentualDefeituoso,
        List<RelatorioProducaoItemDTO> detalhes,
        List<FeriadoDTO> feriados,
        boolean consultaFeriadosConcluida
) {
}
