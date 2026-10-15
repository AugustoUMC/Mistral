
package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.FeriadoDTO;
import com.tcc.sistema_producao.dto.RelatorioProducaoDTO;
import com.tcc.sistema_producao.dto.RelatorioProducaoItemDTO;
import com.tcc.sistema_producao.entidade.MetaProducao;
import com.tcc.sistema_producao.entidade.RegistroProducao;
import com.tcc.sistema_producao.repositorio.MetaProducaoRepositorio;
import com.tcc.sistema_producao.repositorio.RegistroProducaoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioProducaoServico {

    private final RegistroProducaoRepositorio registroRepositorio;
    private final MetaProducaoRepositorio metaRepositorio;
    private final FeriadoServico feriadoServico;

    public RelatorioProducaoServico(
            RegistroProducaoRepositorio registroRepositorio,
            MetaProducaoRepositorio metaRepositorio,
            FeriadoServico feriadoServico) {

        this.registroRepositorio = registroRepositorio;
        this.metaRepositorio = metaRepositorio;
        this.feriadoServico = feriadoServico;
    }

    @Transactional(readOnly = true)
    public RelatorioProducaoDTO gerarRelatorioGlobal(
            LocalDate dataInicial,
            LocalDate dataFinal) {

        validarPeriodo(dataInicial, dataFinal);

        long quantidadeMeses =
                ChronoUnit.MONTHS.between(
                        YearMonth.from(dataInicial),
                        YearMonth.from(dataFinal)
                ) + 1;

        return gerarResumo(
                dataInicial,
                dataFinal,
                quantidadeMeses
        );
    }

    @Transactional(readOnly = true)
    public RelatorioProducaoDTO gerarRelatorioMensal(
            int ano,
            int mes) {

        YearMonth periodo;

        try {
            periodo = YearMonth.of(ano, mes);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException(
                    "Informe um ano e mês válidos"
            );
        }

        return gerarResumo(
                periodo.atDay(1),
                periodo.atEndOfMonth(),
                1
        );
    }

    private RelatorioProducaoDTO gerarResumo(
            LocalDate dataInicial,
            LocalDate dataFinal,
            long multiplicadorMeta) {

        Map<ChaveGrupoProduto, Acumulado> acumulados =
                new LinkedHashMap<>();

        for (MetaProducao meta : metaRepositorio.findAll()) {

            var grupo = meta.getGrupoProducao();
            var produto = meta.getProduto();

            ChaveGrupoProduto chave =
                    new ChaveGrupoProduto(
                            grupo.getId(),
                            produto.getId()
                    );

            Acumulado acumulado = acumulados.computeIfAbsent(
                    chave,
                    k -> new Acumulado(
                            grupo.getId(),
                            grupo.getNome(),
                            produto.getId(),
                            produto.getCodigo(),
                            produto.getDescricao()
                    )
            );

            acumulado.quantidadeMeta +=
                    (long) meta.getQuantidadeMeta() * multiplicadorMeta;
        }

        List<RegistroProducao> registros =
                registroRepositorio
                        .findByDataProducaoBetweenOrderByDataProducaoDesc(
                                dataInicial,
                                dataFinal
                        );

        for (RegistroProducao registro : registros) {

            var grupo = registro.getGrupoProducao();
            var produto = registro.getProduto();

            ChaveGrupoProduto chave =
                    new ChaveGrupoProduto(
                            grupo.getId(),
                            produto.getId()
                    );

            Acumulado acumulado = acumulados.computeIfAbsent(
                    chave,
                    k -> new Acumulado(
                            grupo.getId(),
                            grupo.getNome(),
                            produto.getId(),
                            produto.getCodigo(),
                            produto.getDescricao()
                    )
            );

            acumulado.quantidadeProduzida +=
                    registro.getQuantidadeProduzida();

            acumulado.quantidadeDefeituosa +=
                    registro.getQuantidadeDefeituosa();
        }

        List<RelatorioProducaoItemDTO> detalhes =
                acumulados.values().stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                (Acumulado a) ->
                                                        a.grupoProducaoNome
                                        )
                                        .thenComparing(
                                                a -> a.produtoCodigo
                                        )
                        )
                        .map(this::converterParaDTO)
                        .toList();

        long totalMeta = detalhes.stream()
                .mapToLong(
                        RelatorioProducaoItemDTO::quantidadeMeta
                )
                .sum();

        long totalProduzido = detalhes.stream()
                .mapToLong(
                        RelatorioProducaoItemDTO::quantidadeProduzida
                )
                .sum();

        long totalDefeituoso = detalhes.stream()
                .mapToLong(
                        RelatorioProducaoItemDTO::quantidadeDefeituosa
                )
                .sum();

        ResultadoFeriados resultadoFeriados =
                consultarFeriados(dataInicial, dataFinal);

        return new RelatorioProducaoDTO(
                dataInicial,
                dataFinal,
                totalMeta,
                totalProduzido,
                totalDefeituoso,
                calcularPercentual(totalProduzido, totalMeta),
                calcularPercentual(totalDefeituoso, totalProduzido),
                detalhes,
                resultadoFeriados.feriados(),
                resultadoFeriados.consultaConcluida()
        );
    }

    private ResultadoFeriados consultarFeriados(
            LocalDate dataInicial,
            LocalDate dataFinal) {

        List<FeriadoDTO> feriados = new ArrayList<>();
        boolean consultaConcluida = true;

        for (int ano = dataInicial.getYear();
             ano <= dataFinal.getYear();
             ano++) {

            List<FeriadoDTO> feriadosDoAno;

            try {
                feriadosDoAno = feriadoServico.listarPorAno(ano);
            } catch (RestClientException e) {
                consultaConcluida = false;
                continue;
            }

            if (feriadosDoAno == null) {
                consultaConcluida = false;
                continue;
            }

            for (FeriadoDTO feriado : feriadosDoAno) {

                if (feriado == null || feriado.date() == null) {
                    continue;
                }

                try {
                    LocalDate dataFeriado =
                            LocalDate.parse(feriado.date());

                    if (!dataFeriado.isBefore(dataInicial)
                            && !dataFeriado.isAfter(dataFinal)) {
                        feriados.add(feriado);
                    }

                } catch (DateTimeException e) {
                    continue;
                }
            }
        }

        feriados.sort(
                Comparator.comparing(FeriadoDTO::date)
        );

        return new ResultadoFeriados(
                feriados,
                consultaConcluida
        );
    }

    private RelatorioProducaoItemDTO converterParaDTO(
            Acumulado acumulado) {

        return new RelatorioProducaoItemDTO(
                acumulado.grupoProducaoId,
                acumulado.grupoProducaoNome,
                acumulado.produtoId,
                acumulado.produtoCodigo,
                acumulado.produtoDescricao,
                acumulado.quantidadeMeta,
                acumulado.quantidadeProduzida,
                acumulado.quantidadeDefeituosa,
                calcularPercentual(
                        acumulado.quantidadeProduzida,
                        acumulado.quantidadeMeta
                ),
                calcularPercentual(
                        acumulado.quantidadeDefeituosa,
                        acumulado.quantidadeProduzida
                )
        );
    }

    private BigDecimal calcularPercentual(
            long numerador,
            long denominador) {

        if (denominador <= 0) {
            return null;
        }

        return BigDecimal.valueOf(numerador)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(denominador),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private void validarPeriodo(
            LocalDate dataInicial,
            LocalDate dataFinal) {

        if (dataInicial == null || dataFinal == null) {
            throw new IllegalArgumentException(
                    "As datas inicial e final são obrigatórias"
            );
        }

        if (dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final"
            );
        }
    }

    private record ResultadoFeriados(
            List<FeriadoDTO> feriados,
            boolean consultaConcluida
    ) {
    }

    private record ChaveGrupoProduto(
            Long grupoId,
            Long produtoId
    ) {
    }

    private static class Acumulado {

        private final Long grupoProducaoId;
        private final String grupoProducaoNome;
        private final Long produtoId;
        private final String produtoCodigo;
        private final String produtoDescricao;

        private long quantidadeMeta;
        private long quantidadeProduzida;
        private long quantidadeDefeituosa;

        private Acumulado(
                Long grupoProducaoId,
                String grupoProducaoNome,
                Long produtoId,
                String produtoCodigo,
                String produtoDescricao) {

            this.grupoProducaoId = grupoProducaoId;
            this.grupoProducaoNome = grupoProducaoNome;
            this.produtoId = produtoId;
            this.produtoCodigo = produtoCodigo;
            this.produtoDescricao = produtoDescricao;
        }
    }
}
