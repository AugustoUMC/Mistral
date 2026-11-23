package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.CadastroFuncionarioDTO;
import com.tcc.sistema_producao.dto.EnderecoFuncionarioDTO;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CadastroFuncionarioServicoTest {

    @Mock
    private FuncionarioRepositorio funcionarios;

    @Mock
    private PasswordEncoder codificador;

    @Mock
    private AuditoriaServico auditoria;

    @InjectMocks
    private CadastroFuncionarioServico servico;

    @Test
    void cadastraFuncionarioSemAcessoSemSenha() {
        when(funcionarios.save(any(Funcionario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Funcionario funcionario = servico.cadastrar(dto(false, null, null));

        assertEquals("70", funcionario.getMatricula());
        assertEquals("João", funcionario.getNome());
        assertEquals("Gerente", funcionario.getCargo());
        assertEquals(new BigDecimal("6000.00"), funcionario.getSalario());
        assertTrue(funcionario.getAtivo());
        assertFalse(funcionario.getAcessoSistema());
        assertEquals("USUARIO", funcionario.getPerfil());
        assertNull(funcionario.getSenha());
        verifyNoInteractions(codificador);
        verify(auditoria).registrarUsuarioAtual(
                "CADASTRO_FUNCIONARIO", "Funcionário 70 cadastrado. Acesso ao sistema: desabilitado");
    }

    @Test
    void cadastraFuncionarioComAcessoECodificaSenha() {
        when(codificador.encode("senha-forte-exemplo")).thenReturn("hash-seguro");
        when(funcionarios.save(any(Funcionario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Funcionario funcionario = servico.cadastrar(dto(true, "senha-forte-exemplo", null));

        assertTrue(funcionario.getAcessoSistema());
        assertEquals("hash-seguro", funcionario.getSenha());
        verify(auditoria).registrarUsuarioAtual(
                "CADASTRO_FUNCIONARIO", "Funcionário 70 cadastrado. Acesso ao sistema: habilitado");
    }

    @Test
    void cadastraFuncionarioComAcessoNuloComoDesabilitado() {
        when(funcionarios.save(any(Funcionario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Funcionario funcionario = servico.cadastrar(dto(null, null, null));

        assertFalse(funcionario.getAcessoSistema());
        assertNull(funcionario.getSenha());
    }

    @Test
    void rejeitaMatriculaDuplicada() {
        when(funcionarios.findByMatricula("70")).thenReturn(Optional.of(new Funcionario()));

        assertThrows(IllegalArgumentException.class,
                () -> servico.cadastrar(dto(false, null, null)));

        verify(funcionarios, never()).save(any());
        verifyNoInteractions(auditoria);
    }

    @Test
    void exigeSenhaParaAcessoHabilitado() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.cadastrar(dto(true, null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> servico.cadastrar(dto(true, "  ", null)));

        verify(funcionarios, never()).save(any());
        verifyNoInteractions(codificador);
    }

    @Test
    void normalizaCepUfETextosDoEndereco() {
        EnderecoFuncionarioDTO endereco = new EnderecoFuncionarioDTO(
                "08710100", " Avenida Central ", " 12 ", " ",
                " Centro ", " Mogi das Cruzes ", "sp");
        when(funcionarios.save(any(Funcionario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Funcionario funcionario = servico.cadastrar(dto(false, null, endereco));

        assertEquals("08710-100", funcionario.getCep());
        assertEquals("Avenida Central", funcionario.getLogradouro());
        assertEquals("12", funcionario.getNumero());
        assertNull(funcionario.getComplemento());
        assertEquals("Centro", funcionario.getBairro());
        assertEquals("Mogi das Cruzes", funcionario.getCidade());
        assertEquals("SP", funcionario.getUf());
    }

    @Test
    void preservaCepNaoPadronizadoNaCamadaDeServico() {
        EnderecoFuncionarioDTO endereco = new EnderecoFuncionarioDTO(
                "ABC", null, null, null, null, null, " ");
        when(funcionarios.save(any(Funcionario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Funcionario funcionario = servico.cadastrar(dto(false, null, endereco));

        assertEquals("ABC", funcionario.getCep());
        assertNull(funcionario.getUf());
        assertNull(funcionario.getLogradouro());
    }

    private CadastroFuncionarioDTO dto(Boolean acesso, String senha, EnderecoFuncionarioDTO endereco) {
        return new CadastroFuncionarioDTO(
                "João", "70", "Gerente", new BigDecimal("6000.00"), senha, acesso, endereco);
    }
}
