package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.entidade.Produto;
import com.tcc.sistema_producao.repositorio.ProdutoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServicoTest {

    @Mock
    private ProdutoRepositorio repositorio;

    @Mock
    private AuditoriaServico auditoriaServico;

    @InjectMocks
    private ProdutoServico servico;

    @Test
    void cadastrarRegistraProdutoComPrecoValido() {
        Produto produto = produto("P001", "15.50");
        when(repositorio.save(produto)).thenReturn(produto);

        Produto resultado = servico.cadastrar(produto);

        assertSame(produto, resultado);
        verify(repositorio).save(produto);
        verify(auditoriaServico).registrarUsuarioAtual(
                "CADASTRO_PRODUTO", "Produto P001 cadastrado. Valor bruto unitário: 15.50");
    }

    @Test
    void cadastrarAceitaPrecoNaoInformado() {
        Produto produto = produto("P001", null);
        when(repositorio.save(produto)).thenReturn(produto);

        servico.cadastrar(produto);

        verify(auditoriaServico).registrarUsuarioAtual(
                "CADASTRO_PRODUTO", "Produto P001 cadastrado. Valor bruto unitário: não informado");
    }

    @Test
    void cadastrarRejeitaCodigoDuplicado() {
        Produto produto = produto("P001", "10.00");
        when(repositorio.existsByCodigo("P001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(produto));

        verify(repositorio, never()).save(any());
    }

    @Test
    void cadastrarRejeitaPrecoNegativo() {
        assertPrecoInvalido("-0.01");
    }

    @Test
    void cadastrarRejeitaMaisDeDuasCasasDecimais() {
        assertPrecoInvalido("10.123");
    }

    @Test
    void cadastrarRejeitaMaisDeDezDigitosInteiros() {
        assertPrecoInvalido("10000000000.00");
    }

    @Test
    void cadastrarAceitaZerosAdicionaisSemAlterarValor() {
        Produto produto = produto("P001", "10.000");
        when(repositorio.save(produto)).thenReturn(produto);

        assertDoesNotThrow(() -> servico.cadastrar(produto));
    }

    @Test
    void listarRetornaProdutos() {
        Produto produto = produto("P001", "10.00");
        when(repositorio.findAll()).thenReturn(List.of(produto));

        assertEquals(List.of(produto), servico.listar());
    }

    @Test
    void buscarPorIdRetornaProdutoExistente() {
        Produto produto = produto("P001", null);
        when(repositorio.findById(4L)).thenReturn(Optional.of(produto));

        assertSame(produto, servico.buscarPorId(4L));
    }

    @Test
    void buscarPorIdRejeitaProdutoAusente() {
        when(repositorio.findById(4L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servico.buscarPorId(4L));
    }

    @Test
    void atualizarTransfereDadosERegistraMudancaDePreco() {
        Produto salvo = produto("P001", "12.00");
        Produto dados = produto("P002", "18.50");
        dados.setDescricao("Novo produto");
        dados.setEan("789123");
        dados.setDun("123456");
        dados.setCaixaMaster(20);
        dados.setDiametroCm(new BigDecimal("5.5"));
        dados.setAltura(new BigDecimal("12.0"));
        dados.setImagem("produto.png");

        when(repositorio.findById(1L)).thenReturn(Optional.of(salvo));
        when(repositorio.save(salvo)).thenReturn(salvo);

        Produto atualizado = servico.atualizar(1L, dados);

        assertSame(salvo, atualizado);
        assertEquals("P002", atualizado.getCodigo());
        assertEquals("Novo produto", atualizado.getDescricao());
        assertEquals("789123", atualizado.getEan());
        assertEquals("123456", atualizado.getDun());
        assertEquals(20, atualizado.getCaixaMaster());
        assertEquals(new BigDecimal("5.5"), atualizado.getDiametroCm());
        assertEquals(new BigDecimal("12.0"), atualizado.getAltura());
        assertEquals(new BigDecimal("18.50"), atualizado.getPrecoBruto());
        assertEquals("produto.png", atualizado.getImagem());
        verify(auditoriaServico).registrarUsuarioAtual(
                "ALTERACAO_PRODUTO",
                "Produto P001 alterado para P002. Valor bruto unitário: 12.00 -> 18.50");
    }

    @Test
    void atualizarPermiteManterMesmoCodigo() {
        Produto salvo = produto("P001", null);
        when(repositorio.findById(1L)).thenReturn(Optional.of(salvo));
        when(repositorio.save(salvo)).thenReturn(salvo);

        servico.atualizar(1L, produto("P001", "9.99"));

        verify(repositorio, never()).existsByCodigo(any());
        verify(repositorio).save(salvo);
    }

    @Test
    void atualizarRejeitaCodigoUtilizadoPorOutroProduto() {
        Produto salvo = produto("P001", "10.00");
        when(repositorio.findById(1L)).thenReturn(Optional.of(salvo));
        when(repositorio.existsByCodigo("P002")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> servico.atualizar(1L, produto("P002", "10.00")));

        verify(repositorio, never()).save(any());
    }

    @Test
    void atualizarRejeitaNovoPrecoInvalido() {
        Produto salvo = produto("P001", "10.00");
        when(repositorio.findById(1L)).thenReturn(Optional.of(salvo));

        assertThrows(IllegalArgumentException.class,
                () -> servico.atualizar(1L, produto("P001", "-1.00")));

        verify(repositorio, never()).save(any());
    }

    @Test
    void excluirRemoveProdutoERegistraAuditoria() {
        Produto produto = produto("P001", "15.50");
        when(repositorio.findById(1L)).thenReturn(Optional.of(produto));

        servico.excluir(1L);

        verify(repositorio).delete(produto);
        verify(auditoriaServico).registrarUsuarioAtual(
                "EXCLUSAO_PRODUTO", "Produto P001 excluído");
    }

    private void assertPrecoInvalido(String valor) {
        Produto produto = produto("P001", valor);

        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(produto));

        verify(repositorio, never()).save(any());
    }

    private Produto produto(String codigo, String preco) {
        Produto produto = new Produto();
        produto.setCodigo(codigo);
        produto.setDescricao("Produto de vidro");
        if (preco != null) {
            produto.setPrecoBruto(new BigDecimal(preco));
        }
        return produto;
    }
}
