package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.entidade.Produto;
import com.tcc.sistema_producao.repositorio.ProdutoRepositorio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProdutoServico {

    private final ProdutoRepositorio produtoRepositorio;
    private final AuditoriaServico auditoriaServico;

    public ProdutoServico(
            ProdutoRepositorio produtoRepositorio,
            AuditoriaServico auditoriaServico) {

        this.produtoRepositorio = produtoRepositorio;
        this.auditoriaServico = auditoriaServico;
    }

    public Produto cadastrar(Produto produto) {

        if (produtoRepositorio.existsByCodigo(produto.getCodigo())) {
            throw new IllegalArgumentException(
                    "O código do produto já está cadastrado"
            );
        }

        validarPrecoBruto(produto.getPrecoBruto());

        Produto produtoSalvo =
                produtoRepositorio.save(produto);

        auditoriaServico.registrarUsuarioAtual(
                "CADASTRO_PRODUTO",
                "Produto " + produtoSalvo.getCodigo()
                        + " cadastrado. Valor bruto unitário: "
                        + formatarPreco(produtoSalvo.getPrecoBruto())
        );

        return produtoSalvo;
    }

    public List<Produto> listar() {
        return produtoRepositorio.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepositorio.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Produto não encontrado"
                        ));
    }

    public Produto atualizar(Long id, Produto dados) {

        Produto produto = buscarPorId(id);

        if (!produto.getCodigo().equals(dados.getCodigo())
                && produtoRepositorio.existsByCodigo(dados.getCodigo())) {

            throw new IllegalArgumentException(
                    "O código do produto já está cadastrado"
            );
        }

        String codigoAnterior = produto.getCodigo();
        BigDecimal precoAnterior = produto.getPrecoBruto();

        validarPrecoBruto(dados.getPrecoBruto());

        produto.setCodigo(dados.getCodigo());
        produto.setDescricao(dados.getDescricao());
        produto.setEan(dados.getEan());
        produto.setDun(dados.getDun());
        produto.setCaixaMaster(dados.getCaixaMaster());
        produto.setDiametroCm(dados.getDiametroCm());
        produto.setAltura(dados.getAltura());
        produto.setPrecoBruto(dados.getPrecoBruto());
        produto.setImagem(dados.getImagem());

        Produto produtoAtualizado =
                produtoRepositorio.save(produto);

        auditoriaServico.registrarUsuarioAtual(
                "ALTERACAO_PRODUTO",
                "Produto " + codigoAnterior
                        + " alterado para "
                        + produtoAtualizado.getCodigo()
                        + ". Valor bruto unitário: "
                        + formatarPreco(precoAnterior)
                        + " -> "
                        + formatarPreco(produtoAtualizado.getPrecoBruto())
        );

        return produtoAtualizado;
    }

    public void excluir(Long id) {

        Produto produto = buscarPorId(id);

        produtoRepositorio.delete(produto);

        auditoriaServico.registrarUsuarioAtual(
                "EXCLUSAO_PRODUTO",
                "Produto " + produto.getCodigo() + " excluído"
        );
    }

    private void validarPrecoBruto(BigDecimal precoBruto) {
        if (precoBruto == null) {
            return;
        }

        if (precoBruto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor bruto unitário não pode ser negativo."
            );
        }

        if (precoBruto.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "O valor bruto unitário deve ter no máximo duas casas decimais."
            );
        }

        if (precoBruto.precision() - precoBruto.scale() > 10) {
            throw new IllegalArgumentException(
                    "O valor bruto unitário excede o limite permitido."
            );
        }
    }

    private String formatarPreco(BigDecimal preco) {
        return preco == null ? "não informado" : preco.toPlainString();
    }
}