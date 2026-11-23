package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.entidade.MetaProducao;
import com.tcc.sistema_producao.entidade.Produto;
import com.tcc.sistema_producao.repositorio.GrupoProducaoRepositorio;
import com.tcc.sistema_producao.repositorio.MetaProducaoRepositorio;
import com.tcc.sistema_producao.repositorio.ProdutoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetaProducaoServicoTest {

    @Mock
    private MetaProducaoRepositorio metas;

    @Mock
    private GrupoProducaoRepositorio grupos;

    @Mock
    private ProdutoRepositorio produtos;

    @Mock
    private AuditoriaServico auditoria;

    @InjectMocks
    private MetaProducaoServico servico;

    @Test
    void cadastrarVinculaGrupoProdutoEQuantidade() {
        GrupoProducao grupo = grupo();
        Produto produto = produto();
        when(grupos.findById(1L)).thenReturn(Optional.of(grupo));
        when(produtos.findById(2L)).thenReturn(Optional.of(produto));
        when(metas.save(any(MetaProducao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        MetaProducao meta = servico.cadastrar(1L, 2L, 1000);

        assertSame(grupo, meta.getGrupoProducao());
        assertSame(produto, meta.getProduto());
        assertEquals(1000, meta.getQuantidadeMeta());
        verify(auditoria).registrarUsuarioAtual(
                "CADASTRO_META", "Meta de 1000 para o grupo Grupo A e produto P001 cadastrada");
    }

    @Test
    void cadastrarRejeitaQuantidadeNulaOuNaoPositiva() {
        for (Integer quantidade : new Integer[]{null, 0, -1}) {
            assertThrows(IllegalArgumentException.class,
                    () -> servico.cadastrar(1L, 2L, quantidade));
        }
        verifyNoInteractions(metas, grupos, produtos, auditoria);
    }

    @Test
    void cadastrarRejeitaMetaDuplicada() {
        when(metas.existsByGrupoProducaoIdAndProdutoId(1L, 2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(1L, 2L, 500));
        verify(metas, never()).save(any());
    }

    @Test
    void cadastrarRejeitaGrupoInexistente() {
        when(grupos.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(1L, 2L, 500));
        verify(metas, never()).save(any());
    }

    @Test
    void cadastrarRejeitaProdutoInexistente() {
        when(grupos.findById(1L)).thenReturn(Optional.of(grupo()));
        when(produtos.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(1L, 2L, 500));
        verify(metas, never()).save(any());
    }

    @Test
    void listarRetornaMetas() {
        MetaProducao meta = meta();
        when(metas.findAll()).thenReturn(List.of(meta));

        assertEquals(List.of(meta), servico.listar());
    }

    @Test
    void buscarPorIdRetornaMetaExistente() {
        MetaProducao meta = meta();
        when(metas.findById(3L)).thenReturn(Optional.of(meta));

        assertSame(meta, servico.buscarPorId(3L));
    }

    @Test
    void buscarPorIdRejeitaMetaInexistente() {
        when(metas.findById(3L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servico.buscarPorId(3L));
    }

    @Test
    void listarPorGrupoRetornaSomenteMetasDoGrupo() {
        MetaProducao meta = meta();
        when(metas.findByGrupoProducaoId(1L)).thenReturn(List.of(meta));

        assertEquals(List.of(meta), servico.listarPorGrupo(1L));
    }

    @Test
    void atualizarAlteraQuantidadeERegistraAuditoria() {
        MetaProducao meta = meta();
        when(metas.findById(3L)).thenReturn(Optional.of(meta));
        when(grupos.findById(1L)).thenReturn(Optional.of(grupo()));
        when(produtos.findById(2L)).thenReturn(Optional.of(produto()));
        when(metas.save(meta)).thenReturn(meta);

        assertSame(meta, servico.atualizar(3L, 1L, 2L, 2000));
        assertEquals(2000, meta.getQuantidadeMeta());
        verify(auditoria).registrarUsuarioAtual(
                "ALTERACAO_META", "Meta do grupo Grupo A e produto P001 alterada para 2000");
    }

    @Test
    void atualizarRejeitaQuantidadeInvalida() {
        when(metas.findById(3L)).thenReturn(Optional.of(meta()));

        assertThrows(IllegalArgumentException.class, () -> servico.atualizar(3L, 1L, 2L, 0));
        verify(metas, never()).save(any());
    }

    @Test
    void atualizarRejeitaDuplicidadeEmOutraMeta() {
        when(metas.findById(3L)).thenReturn(Optional.of(meta()));
        when(metas.existsByGrupoProducaoIdAndProdutoIdAndIdNot(1L, 2L, 3L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> servico.atualizar(3L, 1L, 2L, 300));
        verify(metas, never()).save(any());
    }

    @Test
    void atualizarRejeitaGrupoInexistente() {
        when(metas.findById(3L)).thenReturn(Optional.of(meta()));
        when(grupos.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servico.atualizar(3L, 1L, 2L, 300));
    }

    @Test
    void atualizarRejeitaProdutoInexistente() {
        when(metas.findById(3L)).thenReturn(Optional.of(meta()));
        when(grupos.findById(1L)).thenReturn(Optional.of(grupo()));
        when(produtos.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servico.atualizar(3L, 1L, 2L, 300));
    }

    @Test
    void excluirRemoveMetaERegistraAuditoria() {
        MetaProducao meta = meta();
        when(metas.findById(3L)).thenReturn(Optional.of(meta));

        servico.excluir(3L);

        verify(metas).delete(meta);
        verify(auditoria).registrarUsuarioAtual(
                "EXCLUSAO_META", "Meta do grupo Grupo A e produto P001 excluída");
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

    private MetaProducao meta() {
        MetaProducao meta = new MetaProducao();
        meta.setId(3L);
        meta.setGrupoProducao(grupo());
        meta.setProduto(produto());
        meta.setQuantidadeMeta(1000);
        return meta;
    }
}
