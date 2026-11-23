package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.RegistroProducaoDTO;
import com.tcc.sistema_producao.dto.RegistroProducaoRespostaDTO;
import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.entidade.Produto;
import com.tcc.sistema_producao.entidade.RegistroProducao;
import com.tcc.sistema_producao.repositorio.GrupoProducaoRepositorio;
import com.tcc.sistema_producao.repositorio.ProdutoRepositorio;
import com.tcc.sistema_producao.repositorio.RegistroProducaoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroProducaoServicoTest {

    private static final LocalDate DATA = LocalDate.of(2026, 11, 21);

    @Mock
    private RegistroProducaoRepositorio registros;

    @Mock
    private GrupoProducaoRepositorio grupos;

    @Mock
    private ProdutoRepositorio produtos;

    @Mock
    private AuditoriaServico auditoria;

    @InjectMocks
    private RegistroProducaoServico servico;

    @Test
    void cadastrarCalculaPercentualEAuditaOperacao() {
        when(grupos.findById(1L)).thenReturn(Optional.of(grupo()));
        when(produtos.findById(2L)).thenReturn(Optional.of(produto()));
        when(registros.save(any(RegistroProducao.class))).thenAnswer(invocacao -> {
            RegistroProducao registro = invocacao.getArgument(0);
            registro.setId(3L);
            return registro;
        });

        RegistroProducaoRespostaDTO resposta = servico.cadastrar(dto(500, 10));

        assertEquals(3L, resposta.id());
        assertEquals(1L, resposta.grupoProducaoId());
        assertEquals("Grupo A", resposta.grupoProducaoNome());
        assertEquals(2L, resposta.produtoId());
        assertEquals("P001", resposta.produtoCodigo());
        assertEquals(DATA, resposta.dataProducao());
        assertEquals(500, resposta.quantidadeProduzida());
        assertEquals(10, resposta.quantidadeDefeituosa());
        assertEquals(new BigDecimal("15.50"), resposta.precoBrutoUnitario());
        assertEquals(new BigDecimal("2.00"), resposta.percentualDefeituoso());
        verify(auditoria).registrarUsuarioAtual(
                "REGISTRO_PRODUCAO", "Produção de 500 peças registrada para o grupo Grupo A e produto P001");
    }

    @Test
    void cadastrarRejeitaDefeituososAcimaDaProducao() {
        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(dto(10, 11)));

        verifyNoInteractions(grupos, produtos, registros, auditoria);
    }

    @Test
    void cadastrarRejeitaGrupoInexistente() {
        when(grupos.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(dto(10, 1)));
        verify(registros, never()).save(any());
    }

    @Test
    void cadastrarRejeitaProdutoInexistente() {
        when(grupos.findById(1L)).thenReturn(Optional.of(grupo()));
        when(produtos.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(dto(10, 1)));
        verify(registros, never()).save(any());
    }

    @Test
    void listarOrdenadoConvertePercentualArredondado() {
        RegistroProducao registro = registro(3, 1);
        when(registros.findAllByOrderByDataProducaoDesc()).thenReturn(List.of(registro));

        List<RegistroProducaoRespostaDTO> resposta = servico.listar();

        assertEquals(1, resposta.size());
        assertEquals(new BigDecimal("33.33"), resposta.get(0).percentualDefeituoso());
    }

    @Test
    void relatorioRejeitaDatasAusentes() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.relatorioPorGrupoEPeriodo(1L, null, DATA));
        assertThrows(IllegalArgumentException.class,
                () -> servico.relatorioPorGrupoEPeriodo(1L, DATA, null));

        verifyNoInteractions(grupos, registros);
    }

    @Test
    void relatorioRejeitaPeriodoInvertido() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.relatorioPorGrupoEPeriodo(1L, DATA.plusDays(1), DATA));

        verifyNoInteractions(grupos, registros);
    }

    @Test
    void relatorioRejeitaGrupoInexistente() {
        when(grupos.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> servico.relatorioPorGrupoEPeriodo(1L, DATA.minusDays(1), DATA));
        verifyNoInteractions(registros);
    }

    @Test
    void relatorioConsultaPeriodoECalculaPercentual() {
        LocalDate inicio = DATA.minusDays(7);
        when(grupos.findById(1L)).thenReturn(Optional.of(grupo()));
        when(registros.findByGrupoProducao_IdAndDataProducaoBetweenOrderByDataProducaoDesc(
                1L, inicio, DATA)).thenReturn(List.of(registro(200, 25)));

        List<RegistroProducaoRespostaDTO> resposta = servico.relatorioPorGrupoEPeriodo(1L, inicio, DATA);

        assertEquals(1, resposta.size());
        assertEquals(new BigDecimal("12.50"), resposta.get(0).percentualDefeituoso());
    }

    private RegistroProducaoDTO dto(int produzidas, int defeituosas) {
        return new RegistroProducaoDTO(1L, 2L, DATA, produzidas, defeituosas);
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
        produto.setPrecoBruto(new BigDecimal("15.50"));
        return produto;
    }

    private RegistroProducao registro(int produzidas, int defeituosas) {
        RegistroProducao registro = new RegistroProducao();
        registro.setId(3L);
        registro.setGrupoProducao(grupo());
        registro.setProduto(produto());
        registro.setDataProducao(DATA);
        registro.setQuantidadeProduzida(produzidas);
        registro.setQuantidadeDefeituosa(defeituosas);
        registro.setPrecoBrutoUnitario(new BigDecimal("15.50"));
        return registro;
    }
}
