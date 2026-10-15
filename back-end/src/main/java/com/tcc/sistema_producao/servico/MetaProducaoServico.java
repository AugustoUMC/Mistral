package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.entidade.MetaProducao;
import com.tcc.sistema_producao.entidade.Produto;
import com.tcc.sistema_producao.repositorio.GrupoProducaoRepositorio;
import com.tcc.sistema_producao.repositorio.MetaProducaoRepositorio;
import com.tcc.sistema_producao.repositorio.ProdutoRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetaProducaoServico {

    private final MetaProducaoRepositorio metaProducaoRepositorio;
    private final GrupoProducaoRepositorio grupoProducaoRepositorio;
    private final ProdutoRepositorio produtoRepositorio;
    private final AuditoriaServico auditoriaServico;

    public MetaProducaoServico(
            MetaProducaoRepositorio metaProducaoRepositorio,
            GrupoProducaoRepositorio grupoProducaoRepositorio,
            ProdutoRepositorio produtoRepositorio,
            AuditoriaServico auditoriaServico) {

        this.metaProducaoRepositorio = metaProducaoRepositorio;
        this.grupoProducaoRepositorio = grupoProducaoRepositorio;
        this.produtoRepositorio = produtoRepositorio;
        this.auditoriaServico = auditoriaServico;
    }

    public MetaProducao cadastrar(
            Long grupoProducaoId,
            Long produtoId,
            Integer quantidadeMeta) {

        if (quantidadeMeta == null || quantidadeMeta <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade da meta deve ser maior que zero"
            );
        }

        if (metaProducaoRepositorio
                .existsByGrupoProducaoIdAndProdutoId(
                        grupoProducaoId,
                        produtoId)) {

            throw new IllegalArgumentException(
                    "Já existe uma meta para este grupo e produto"
            );
        }

        GrupoProducao grupoProducao = grupoProducaoRepositorio
                .findById(grupoProducaoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Grupo de produção não encontrado"
                        ));

        Produto produto = produtoRepositorio
                .findById(produtoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Produto não encontrado"
                        ));

        MetaProducao metaProducao = new MetaProducao();

        metaProducao.setGrupoProducao(grupoProducao);
        metaProducao.setProduto(produto);
        metaProducao.setQuantidadeMeta(quantidadeMeta);

        MetaProducao metaSalva =
                metaProducaoRepositorio.save(metaProducao);

        auditoriaServico.registrarUsuarioAtual(
                "CADASTRO_META",
                "Meta de "
                        + quantidadeMeta
                        + " para o grupo "
                        + grupoProducao.getNome()
                        + " e produto "
                        + produto.getCodigo()
                        + " cadastrada"
        );

        return metaSalva;
    }

    public List<MetaProducao> listar() {
        return metaProducaoRepositorio.findAll();
    }

    public MetaProducao buscarPorId(Long id) {
        return metaProducaoRepositorio.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Meta de produção não encontrada"
                        ));
    }

    public List<MetaProducao> listarPorGrupo(Long grupoProducaoId) {
        return metaProducaoRepositorio
                .findByGrupoProducaoId(grupoProducaoId);
    }

    public MetaProducao atualizar(
            Long id,
            Long grupoProducaoId,
            Long produtoId,
            Integer quantidadeMeta) {

        MetaProducao metaProducao = buscarPorId(id);

        if (quantidadeMeta == null || quantidadeMeta <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade da meta deve ser maior que zero"
            );
        }

        if (metaProducaoRepositorio
                .existsByGrupoProducaoIdAndProdutoIdAndIdNot(
                        grupoProducaoId,
                        produtoId,
                        id)) {

            throw new IllegalArgumentException(
                    "Já existe uma meta para este grupo e produto"
            );
        }

        GrupoProducao grupoProducao = grupoProducaoRepositorio
                .findById(grupoProducaoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Grupo de produção não encontrado"
                        ));

        Produto produto = produtoRepositorio
                .findById(produtoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Produto não encontrado"
                        ));

        metaProducao.setGrupoProducao(grupoProducao);
        metaProducao.setProduto(produto);
        metaProducao.setQuantidadeMeta(quantidadeMeta);

        MetaProducao metaAtualizada =
                metaProducaoRepositorio.save(metaProducao);

        auditoriaServico.registrarUsuarioAtual(
                "ALTERACAO_META",
                "Meta do grupo "
                        + grupoProducao.getNome()
                        + " e produto "
                        + produto.getCodigo()
                        + " alterada para "
                        + quantidadeMeta
        );

        return metaAtualizada;
    }

    public void excluir(Long id) {

        MetaProducao metaProducao = buscarPorId(id);

        String grupo = metaProducao
                .getGrupoProducao()
                .getNome();

        String produto = metaProducao
                .getProduto()
                .getCodigo();

        metaProducaoRepositorio.delete(metaProducao);

        auditoriaServico.registrarUsuarioAtual(
                "EXCLUSAO_META",
                "Meta do grupo "
                        + grupo
                        + " e produto "
                        + produto
                        + " excluída"
        );
    }
}