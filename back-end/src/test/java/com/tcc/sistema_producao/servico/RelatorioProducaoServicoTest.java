package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.FeriadoDTO;
import com.tcc.sistema_producao.dto.RelatorioProducaoDTO;
import com.tcc.sistema_producao.dto.RelatorioProducaoItemDTO;
import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.entidade.MetaProducao;
import com.tcc.sistema_producao.entidade.Produto;
import com.tcc.sistema_producao.entidade.RegistroProducao;
import com.tcc.sistema_producao.repositorio.MetaProducaoRepositorio;
import com.tcc.sistema_producao.repositorio.RegistroProducaoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioProducaoServicoTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 11, 30);

    @Mock
    private RegistroProducaoRepositorio registros;

    @Mock
    private MetaProducaoRepositorio metas;

    @Mock
    private FeriadoServico feriados;

    @InjectMocks
    private RelatorioProducaoServico servico;

    @Test
    void mensalAgrupaProducaoMetaDefeitosEPercentuais() {
        when(metas.findAll()).thenReturn(List.of(meta(1000)));
        when(registros.findByDataProducaoBetweenOrderByDataProducaoDesc(INICIO, FIM))
                .thenReturn(List.of(registro(200, 20), registro(300, 5)));
        when(feriados.listarPorAno(2026)).thenReturn(List.of());

        RelatorioProducaoDTO relatorio = servico.gerarRelatorioMensal(2026, 11);

        assertEquals(1000L, relatorio.quantidadeMetaTotal());
        assertEquals(500L, relatorio.quantidadeProduzidaTotal());
        assertEquals(25L, relatorio.quantidadeDefeituosaTotal());
        assertEquals(new BigDecimal("50.00"), relatorio.percentualMetaAtingida());
        assertEquals(new BigDecimal("5.00"), relatorio.percentualDefeituoso());
        assertEquals(1, relatorio.detalhes().size());
        RelatorioProducaoItemDTO item = relatorio.detalhes().get(0);
        assertEquals("Grupo A", item.grupoProducaoNome());
        assertEquals("P001", item.produtoCodigo());
        assertEquals(new BigDecimal("50.00"), item.percentualMetaAtingida());
        assertEquals(new BigDecimal("5.00"), item.percentualDefeituoso());
        assertTrue(relatorio.consultaFeriadosConcluida());
    }

    @Test
    void globalMultiplicaMetaPelaQuantidadeDeMeses() {
        LocalDate inicio = LocalDate.of(2026, 10, 15);
        when(metas.findAll()).thenReturn(List.of(meta(100)));
        when(registros.findByDataProducaoBetweenOrderByDataProducaoDesc(inicio, FIM))
                .thenReturn(List.of(registro(50, 2)));
        when(feriados.listarPorAno(2026)).thenReturn(List.of());

        RelatorioProducaoDTO relatorio = servico.gerarRelatorioGlobal(inicio, FIM);

        assertEquals(200L, relatorio.quantidadeMetaTotal());
        assertEquals(50L, relatorio.quantidadeProduzidaTotal());
        assertEquals(new BigDecimal("25.00"), relatorio.percentualMetaAtingida());
        assertEquals(new BigDecimal("4.00"), relatorio.percentualDefeituoso());
    }

    @Test
    void relatorioSemMetasNemRegistrosNaoDividePorZero() {
        when(metas.findAll()).thenReturn(List.of());
        when(registros.findByDataProducaoBetweenOrderByDataProducaoDesc(INICIO, FIM))
                .thenReturn(List.of());
        when(feriados.listarPorAno(2026)).thenReturn(List.of());

        RelatorioProducaoDTO relatorio = servico.gerarRelatorioMensal(2026, 11);

        assertEquals(0L, relatorio.quantidadeMetaTotal());
        assertEquals(0L, relatorio.quantidadeProduzidaTotal());
        assertNull(relatorio.percentualMetaAtingida());
        assertNull(relatorio.percentualDefeituoso());
        assertTrue(relatorio.detalhes().isEmpty());
    }

    @Test
    void relatorioIncluiProducaoSemMeta() {
        when(metas.findAll()).thenReturn(List.of());
        when(registros.findByDataProducaoBetweenOrderByDataProducaoDesc(INICIO, FIM))
                .thenReturn(List.of(registro(100, 10)));
        when(feriados.listarPorAno(2026)).thenReturn(List.of());

        RelatorioProducaoDTO relatorio = servico.gerarRelatorioMensal(2026, 11);

        assertEquals(0L, relatorio.quantidadeMetaTotal());
        assertEquals(100L, relatorio.quantidadeProduzidaTotal());
        assertNull(relatorio.percentualMetaAtingida());
        assertEquals(new BigDecimal("10.00"), relatorio.percentualDefeituoso());
    }

    @Test
    void relatorioIncluiMetaNaoAtingidaSemProducao() {
        when(metas.findAll()).thenReturn(List.of(meta(100)));
        when(registros.findByDataProducaoBetweenOrderByDataProducaoDesc(INICIO, FIM))
                .thenReturn(List.of());
        when(feriados.listarPorAno(2026)).thenReturn(List.of());

        RelatorioProducaoDTO relatorio = servico.gerarRelatorioMensal(2026, 11);

        assertEquals(new BigDecimal("0.00"), relatorio.percentualMetaAtingida());
        assertNull(relatorio.percentualDefeituoso());
    }

    @Test
    void feriadosSaoFiltradosOrdenadosEInvalidosIgnorados() {
        when(metas.findAll()).thenReturn(List.of());
        when(registros.findByDataProducaoBetweenOrderByDataProducaoDesc(INICIO, FIM))
                .thenReturn(List.of());
        FeriadoDTO finados = new FeriadoDTO("2026-11-02", "Finados", "nacional", "segunda-feira");
        FeriadoDTO republica = new FeriadoDTO("2026-11-15", "Proclamação", "nacional", "domingo");
        when(feriados.listarPorAno(2026)).thenReturn(List.of(
                republica,
                new FeriadoDTO("data-invalida", "Inválido", "nacional", ""),
                new FeriadoDTO("2026-12-25", "Natal", "nacional", "sexta-feira"),
                new FeriadoDTO(null, "Sem data", "nacional", ""),
                finados));

        RelatorioProducaoDTO relatorio = servico.gerarRelatorioMensal(2026, 11);

        assertEquals(List.of(finados, republica), relatorio.feriados());
        assertTrue(relatorio.consultaFeriadosConcluida());
    }

    @Test
    void falhaNaApiDeFeriadosNaoImpedeRelatorio() {
        when(metas.findAll()).thenReturn(List.of(meta(100)));
        when(registros.findByDataProducaoBetweenOrderByDataProducaoDesc(INICIO, FIM))
                .thenReturn(List.of(registro(30, 3)));
        when(feriados.listarPorAno(2026)).thenThrow(new RestClientException("Indisponível"));

        RelatorioProducaoDTO relatorio = servico.gerarRelatorioMensal(2026, 11);

        assertFalse(relatorio.consultaFeriadosConcluida());
        assertTrue(relatorio.feriados().isEmpty());
        assertEquals(30L, relatorio.quantidadeProduzidaTotal());
    }

    @Test
    void retornoNuloDaApiDeFeriadosMarcaConsultaIncompleta() {
        when(metas.findAll()).thenReturn(List.of());
        when(registros.findByDataProducaoBetweenOrderByDataProducaoDesc(INICIO, FIM))
                .thenReturn(List.of());
        when(feriados.listarPorAno(2026)).thenReturn(null);

        assertFalse(servico.gerarRelatorioMensal(2026, 11).consultaFeriadosConcluida());
    }

    @Test
    void globalConsultaFeriadosDeTodosOsAnos() {
        LocalDate inicio = LocalDate.of(2025, 12, 1);
        LocalDate fim = LocalDate.of(2026, 1, 31);
        when(metas.findAll()).thenReturn(List.of());
        when(registros.findByDataProducaoBetweenOrderByDataProducaoDesc(inicio, fim))
                .thenReturn(List.of());
        when(feriados.listarPorAno(2025)).thenReturn(List.of());
        when(feriados.listarPorAno(2026)).thenReturn(List.of());

        servico.gerarRelatorioGlobal(inicio, fim);

        verify(feriados).listarPorAno(2025);
        verify(feriados).listarPorAno(2026);
    }

    @Test
    void globalRejeitaDatasInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> servico.gerarRelatorioGlobal(null, FIM));
        assertThrows(IllegalArgumentException.class, () -> servico.gerarRelatorioGlobal(INICIO, null));
        assertThrows(IllegalArgumentException.class,
                () -> servico.gerarRelatorioGlobal(FIM, INICIO));
        verifyNoInteractions(metas, registros, feriados);
    }

    @Test
    void mensalRejeitaMesInvalido() {
        assertThrows(IllegalArgumentException.class, () -> servico.gerarRelatorioMensal(2026, 13));
        verifyNoInteractions(metas, registros, feriados);
    }

    private MetaProducao meta(int quantidade) {
        MetaProducao meta = new MetaProducao();
        meta.setGrupoProducao(grupo());
        meta.setProduto(produto());
        meta.setQuantidadeMeta(quantidade);
        return meta;
    }

    private RegistroProducao registro(int produzidas, int defeituosas) {
        RegistroProducao registro = new RegistroProducao();
        registro.setGrupoProducao(grupo());
        registro.setProduto(produto());
        registro.setQuantidadeProduzida(produzidas);
        registro.setQuantidadeDefeituosa(defeituosas);
        registro.setDataProducao(LocalDate.of(2026, 11, 21));
        return registro;
    }

    private GrupoProducao grupo() {
        GrupoProducao grupo = new GrupoProducao();
        grupo.setId(1L);
        grupo.setNome("Grupo A");
        return grupo;
    }

    private Produto produto() {
        Produto produto = new Produto();
        produto.setId(2L);
        produto.setCodigo("P001");
        produto.setDescricao("Aquário 10cm");
        return produto;
    }
}
