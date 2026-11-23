package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.LoginDTO;
import com.tcc.sistema_producao.dto.LoginRespostaDTO;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServicoTest {

    @Mock
    private AuthenticationManager autenticador;

    @Mock
    private FuncionarioRepositorio funcionarios;

    @Mock
    private JwtServico jwt;

    @Mock
    private AuditoriaServico auditoria;

    @InjectMocks
    private AutenticacaoServico servico;

    @Test
    void loginAutenticaGeraTokenEAudita() {
        Funcionario funcionario = new Funcionario();
        funcionario.setMatricula("70");
        funcionario.setPerfil("USUARIO");
        when(funcionarios.findByMatriculaAndAtivoTrue("70")).thenReturn(Optional.of(funcionario));
        when(jwt.gerarToken("70", "USUARIO")).thenReturn("token-teste");

        LoginRespostaDTO resposta = servico.login(new LoginDTO("70", "senha-ficticia"));

        assertEquals("token-teste", resposta.token());
        verify(autenticador).authenticate(any());
        verify(auditoria).registrar("70", "LOGIN", "Login realizado com sucesso");
    }

    @Test
    void loginRejeitaFuncionarioInativoOuAusente() {
        when(funcionarios.findByMatriculaAndAtivoTrue("70")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> servico.login(new LoginDTO("70", "senha-ficticia")));
        verifyNoInteractions(jwt, auditoria);
    }

    @Test
    void loginFalhaQuandoAutenticacaoNaoAceitaSenha() {
        when(autenticador.authenticate(any())).thenThrow(
                new org.springframework.security.authentication.BadCredentialsException("Credenciais inválidas"));

        assertThrows(AuthenticationException.class,
                () -> servico.login(new LoginDTO("70", "senha-ficticia")));
        verifyNoInteractions(funcionarios, jwt, auditoria);
    }
}
