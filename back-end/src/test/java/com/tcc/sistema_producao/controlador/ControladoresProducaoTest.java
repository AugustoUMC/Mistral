package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.dto.MetaProducaoDTO;
import com.tcc.sistema_producao.dto.RegistroProducaoDTO;
import com.tcc.sistema_producao.dto.RelatorioProducaoDTO;
import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.entidade.MetaProducao;
import com.tcc.sistema_producao.entidade.Produto;
import com.tcc.sistema_producao.servico.FeriadoServico;
import com.tcc.sistema_producao.servico.GrupoProducaoServico;
import com.tcc.sistema_producao.servico.MetaProducaoServico;
import com.tcc.sistema_producao.servico.ProdutoServico;
import com.tcc.sistema_producao.servico.RegistroProducaoServico;
import com.tcc.sistema_producao.servico.RelatorioProducaoServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControladoresProducaoTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 11, 30);

    @Mock
    private GrupoProducaoServico grupos;

    @Mock
    private ProdutoServico produtos;

    @Mock
    private MetaProducaoServico metas;

    @Mock
    private RegistroProducaoServico registros;

    @Mock
    private RelatorioProducaoServico relatorios;

    @Mock
    private FeriadoServico feriados;

    @Test
    void gruposCadastrarSucesso() {
        GrupoProducao grupo = grupo();
        when(grupos.cadastrar(grupo)).thenReturn(grupo);

        ResponseEntity<?> resposta = new GrupoProducaoControlador(grupos).cadastrar(grupo);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertSame(grupo, resposta.getBody());
    }

    @Test
    void gruposCadastrarDuplicidade() {
        GrupoProducao grupo = grupo();
        when(grupos.cadastrar(grupo)).thenThrow(new IllegalArgumentException("Duplicado"));

        ResponseEntity<?> resposta = new GrupoProducaoControlador(grupos).cadastrar(grupo);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals("Duplicado", resposta.getBody());
    }

    @Test
    void gruposListarEBuscar() {
        GrupoProducao grupo = grupo();
        when(grupos.listar()).thenReturn(List.of(grupo));
        when(grupos.buscarPorId(1L)).thenReturn(grupo);
        GrupoProducaoControlador controlador = new GrupoProducaoControlador(grupos);

        assertEquals(List.of(grupo), controlador.listar().getBody());
        assertSame(grupo, controlador.buscarPorId(1L).getBody());
    }

    @Test
    void gruposBuscarInexistente() {
        when(grupos.buscarPorId(1L)).thenThrow(new IllegalArgumentException("Ausente"));

        assertEquals(HttpStatus.NOT_FOUND,
                new GrupoProducaoControlador(grupos).buscarPorId(1L).getStatusCode());
    }

    @Test
    void gruposAtualizarSucessoEErro() {
        GrupoProducao grupo = grupo();
        when(grupos.atualizar(1L, grupo)).thenReturn(grupo).thenThrow(new IllegalArgumentException("Inválido"));
        GrupoProducaoControlador controlador = new GrupoProducaoControlador(grupos);

        assertSame(grupo, controlador.atualizar(1L, grupo).getBody());
        assertEquals(HttpStatus.BAD_REQUEST, controlador.atualizar(1L, grupo).getStatusCode());
    }

    @Test
    void gruposExcluirSucesso() {
        ResponseEntity<?> resposta = new GrupoProducaoControlador(grupos).excluir(1L);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        verify(grupos).excluir(1L);
    }

    @Test
    void gruposExcluirComVinculosRetornaConflito() {
        doThrow(new DataIntegrityViolationException("Vínculo existente")).when(grupos).excluir(1L);

        ResponseEntity<?> resposta = new GrupoProducaoControlador(grupos).excluir(1L);

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
    }

    @Test
    void gruposExcluirInexistenteRetornaNaoEncontrado() {
        doThrow(new IllegalArgumentException("Ausente")).when(grupos).excluir(1L);

        assertEquals(HttpStatus.NOT_FOUND,
                new GrupoProducaoControlador(grupos).excluir(1L).getStatusCode());
    }

    @Test
    void produtosCadastrarSucessoEDuplicidade() {
        Produto produto = produto();
        when(produtos.cadastrar(produto)).thenReturn(produto)
                .thenThrow(new IllegalArgumentException("Duplicado"));
        ProdutoControlador controlador = new ProdutoControlador(produtos);

        assertEquals(HttpStatus.CREATED, controlador.cadastrar(produto).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, controlador.cadastrar(produto).getStatusCode());
    }

    @Test
    void produtosListarEBuscar() {
        Produto produto = produto();
        when(produtos.listar()).thenReturn(List.of(produto));
        when(produtos.buscarPorId(2L)).thenReturn(produto);
        ProdutoControlador controlador = new ProdutoControlador(produtos);

        assertEquals(List.of(produto), controlador.listar().getBody());
        assertSame(produto, controlador.buscarPorId(2L).getBody());
    }

    @Test
    void produtosBuscarInexistente() {
        when(produtos.buscarPorId(2L)).thenThrow(new IllegalArgumentException("Ausente"));

        assertEquals(HttpStatus.NOT_FOUND,
                new ProdutoControlador(produtos).buscarPorId(2L).getStatusCode());
    }

    @Test
    void produtosAtualizarSucessoEErro() {
        Produto produto = produto();
        when(produtos.atualizar(2L, produto)).thenReturn(produto)
                .thenThrow(new IllegalArgumentException("Inválido"));
        ProdutoControlador controlador = new ProdutoControlador(produtos);

        assertSame(produto, controlador.atualizar(2L, produto).getBody());
        assertEquals(HttpStatus.BAD_REQUEST, controlador.atualizar(2L, produto).getStatusCode());
    }

    @Test
    void produtosExcluirSucessoENaoEncontrado() {
        ProdutoControlador controlador = new ProdutoControlador(produtos);
        assertEquals(HttpStatus.NO_CONTENT, controlador.excluir(2L).getStatusCode());
        doThrow(new IllegalArgumentException("Ausente")).when(produtos).excluir(3L);

        assertEquals(HttpStatus.NOT_FOUND, controlador.excluir(3L).getStatusCode());
    }

    @Test
    void metasCadastrarSucessoEErro() {
        MetaProducaoDTO dto = metaDTO();
        MetaProducao meta = new MetaProducao();
        when(metas.cadastrar(1L, 2L, 1000)).thenReturn(meta)
                .thenThrow(new IllegalArgumentException("Duplicada"));
        MetaProducaoControlador controlador = new MetaProducaoControlador(metas);

        assertEquals(HttpStatus.CREATED, controlador.cadastrar(dto).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controlador.cadastrar(dto).getStatusCode());
    }

    @Test
    void metasListarBuscarEListarPorGrupo() {
        MetaProducao meta = new MetaProducao();
        when(metas.listar()).thenReturn(List.of(meta));
        when(metas.buscarPorId(3L)).thenReturn(meta);
        when(metas.listarPorGrupo(1L)).thenReturn(List.of(meta));
        MetaProducaoControlador controlador = new MetaProducaoControlador(metas);

        assertEquals(List.of(meta), controlador.listar().getBody());
        assertSame(meta, controlador.buscarPorId(3L).getBody());
        assertEquals(List.of(meta), controlador.listarPorGrupo(1L).getBody());
    }

    @Test
    void metasBuscarInexistente() {
        when(metas.buscarPorId(3L)).thenThrow(new IllegalArgumentException("Ausente"));

        assertEquals(HttpStatus.NOT_FOUND,
                new MetaProducaoControlador(metas).buscarPorId(3L).getStatusCode());
    }

    @Test
    void metasAtualizarSucessoEErro() {
        MetaProducao meta = new MetaProducao();
        when(metas.atualizar(3L, 1L, 2L, 1000)).thenReturn(meta)
                .thenThrow(new IllegalArgumentException("Inválida"));
        MetaProducaoControlador controlador = new MetaProducaoControlador(metas);

        assertSame(meta, controlador.atualizar(3L, metaDTO()).getBody());
        assertEquals(HttpStatus.BAD_REQUEST, controlador.atualizar(3L, metaDTO()).getStatusCode());
    }

    @Test
    void metasExcluirSucessoENaoEncontrado() {
        MetaProducaoControlador controlador = new MetaProducaoControlador(metas);
        assertEquals(HttpStatus.NO_CONTENT, controlador.excluir(3L).getStatusCode());
        doThrow(new IllegalArgumentException("Ausente")).when(metas).excluir(4L);

        assertEquals(HttpStatus.NOT_FOUND, controlador.excluir(4L).getStatusCode());
    }

    @Test
    void registroCadastrarSucessoEErro() {
        RegistroProducaoDTO dto = new RegistroProducaoDTO(1L, 2L, INICIO, 100, 5);
        when(registros.cadastrar(dto)).thenReturn(null).thenThrow(new IllegalArgumentException("Inválido"));
        RegistroProducaoControlador controlador = new RegistroProducaoControlador(registros);

        assertEquals(HttpStatus.CREATED, controlador.cadastrar(dto).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controlador.cadastrar(dto).getStatusCode());
    }

    @Test
    void registroListarEConsultarPeriodo() {
        when(registros.listar()).thenReturn(List.of());
        when(registros.relatorioPorGrupoEPeriodo(1L, INICIO, FIM)).thenReturn(List.of());
        RegistroProducaoControlador controlador = new RegistroProducaoControlador(registros);

        assertEquals(List.of(), controlador.listar().getBody());
        assertEquals(HttpStatus.OK,
                controlador.relatorioPorGrupoEPeriodo(1L, INICIO, FIM).getStatusCode());
    }

    @Test
    void registroConsultaComPeriodoInvalido() {
        when(registros.relatorioPorGrupoEPeriodo(1L, INICIO, FIM))
                .thenThrow(new IllegalArgumentException("Datas inválidas"));

        assertEquals(HttpStatus.BAD_REQUEST,
                new RegistroProducaoControlador(registros)
                        .relatorioPorGrupoEPeriodo(1L, INICIO, FIM).getStatusCode());
    }

    @Test
    void registroConsultaComGrupoInexistente() {
        when(registros.relatorioPorGrupoEPeriodo(1L, INICIO, FIM))
                .thenThrow(new NoSuchElementException("Ausente"));

        assertEquals(HttpStatus.NOT_FOUND,
                new RegistroProducaoControlador(registros)
                        .relatorioPorGrupoEPeriodo(1L, INICIO, FIM).getStatusCode());
    }

    @Test
    void relatoriosGlobalSucessoEErro() {
        RelatorioProducaoDTO relatorio = mock(RelatorioProducaoDTO.class);
        when(relatorios.gerarRelatorioGlobal(INICIO, FIM)).thenReturn(relatorio)
                .thenThrow(new IllegalArgumentException("Datas inválidas"));
        RelatorioProducaoControlador controlador = new RelatorioProducaoControlador(relatorios);

        assertSame(relatorio, controlador.gerarRelatorioGlobal(INICIO, FIM).getBody());
        assertEquals(HttpStatus.BAD_REQUEST,
                controlador.gerarRelatorioGlobal(INICIO, FIM).getStatusCode());
    }

    @Test
    void relatoriosMensalSucessoEErro() {
        RelatorioProducaoDTO relatorio = mock(RelatorioProducaoDTO.class);
        when(relatorios.gerarRelatorioMensal(2026, 11)).thenReturn(relatorio)
                .thenThrow(new IllegalArgumentException("Mês inválido"));
        RelatorioProducaoControlador controlador = new RelatorioProducaoControlador(relatorios);

        assertSame(relatorio, controlador.gerarRelatorioMensal(2026, 11).getBody());
        assertEquals(HttpStatus.BAD_REQUEST,
                controlador.gerarRelatorioMensal(2026, 11).getStatusCode());
    }

    @Test
    void feriadosListarPorAno() {
        when(feriados.listarPorAno(2026)).thenReturn(List.of());

        assertEquals(List.of(), new FeriadoControlador(feriados).listar(2026).getBody());
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
        return produto;
    }

    private MetaProducaoDTO metaDTO() {
        return new MetaProducaoDTO(1L, 2L, 1000);
    }
}
