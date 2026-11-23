package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.EnderecoFuncionarioDTO;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioAdministracaoServicoTest {

    @Mock
    private FuncionarioRepositorio funcionarios;

    @Mock
    private AuditoriaServico auditoria;

    @Mock
    private AlocacaoFuncionarioGrupoServico alocacao;

    @InjectMocks
    private FuncionarioAdministracaoServico servico;

    @Test
    void inativarFuncionarioEncerraVinculosEAudita() {
        Funcionario funcionario = funcionario(true);
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario));
        when(alocacao.encerrarAssociacoesAtuais(1L)).thenReturn(2);

        servico.inativar(1L);

        assertFalse(funcionario.getAtivo());
        verify(funcionarios).save(funcionario);
        verify(auditoria).registrarUsuarioAtual(
                "INATIVACAO_FUNCIONARIO", "Funcionário 70 inativado. Associações atuais encerradas: 2.");
    }

    @Test
    void inativarNovamenteNaoDuplicaAuditoria() {
        Funcionario funcionario = funcionario(false);
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario));

        servico.inativar(1L);

        verify(funcionarios, never()).save(any());
        verifyNoInteractions(alocacao, auditoria);
    }

    @Test
    void inativarFuncionarioInexistenteRejeita() {
        when(funcionarios.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servico.inativar(1L));
        verifyNoInteractions(alocacao, auditoria);
    }

    @Test
    void consultarEnderecoRetornaDadosPersistidos() {
        Funcionario funcionario = funcionario(true);
        funcionario.setCep("08710-100");
        funcionario.setUf("SP");
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario));

        EnderecoFuncionarioDTO endereco = servico.consultarEndereco(1L);

        assertEquals("08710-100", endereco.cep());
        assertEquals("SP", endereco.uf());
    }

    @Test
    void atualizarEnderecoNormalizaCampos() {
        Funcionario funcionario = funcionario(true);
        EnderecoFuncionarioDTO dados = new EnderecoFuncionarioDTO(
                "08710100", " Rua A ", " 12 ", "  ", " Centro ", " Mogi ", "sp");
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario));
        when(funcionarios.save(funcionario)).thenReturn(funcionario);

        EnderecoFuncionarioDTO resultado = servico.atualizarEndereco(1L, dados);

        assertEquals("08710-100", resultado.cep());
        assertEquals("Rua A", resultado.logradouro());
        assertEquals("12", resultado.numero());
        assertNull(resultado.complemento());
        assertEquals("Centro", resultado.bairro());
        assertEquals("Mogi", resultado.cidade());
        assertEquals("SP", resultado.uf());
        verify(auditoria).registrarUsuarioAtual(
                "ATUALIZACAO_ENDERECO_FUNCIONARIO", "Endereço do funcionário 70 atualizado.");
    }

    @Test
    void atualizarEnderecoPermiteCamposNulos() {
        Funcionario funcionario = funcionario(true);
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario));
        when(funcionarios.save(funcionario)).thenReturn(funcionario);

        EnderecoFuncionarioDTO resultado = servico.atualizarEndereco(1L,
                new EnderecoFuncionarioDTO(null, null, null, null, null, null, null));

        assertNull(resultado.cep());
        assertNull(resultado.uf());
    }

    @Test
    void atualizarEnderecoComCepForaDoFormatoPreservaTexto() {
        Funcionario funcionario = funcionario(true);
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario));
        when(funcionarios.save(funcionario)).thenReturn(funcionario);

        EnderecoFuncionarioDTO resultado = servico.atualizarEndereco(1L,
                new EnderecoFuncionarioDTO(" ABC ", null, null, null, null, null, " "));

        assertEquals("ABC", resultado.cep());
        assertNull(resultado.uf());
    }

    private Funcionario funcionario(boolean ativo) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setMatricula("70");
        funcionario.setAtivo(ativo);
        return funcionario;
    }
}
