package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.dto.CadastroFuncionarioDTO;
import com.tcc.sistema_producao.dto.LoginDTO;
import com.tcc.sistema_producao.dto.LoginRespostaDTO;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.servico.AutenticacaoServico;
import com.tcc.sistema_producao.servico.CadastroFuncionarioServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoControladorTest {

    @Mock
    private AutenticacaoServico autenticacao;

    @Mock
    private CadastroFuncionarioServico cadastro;

    @InjectMocks
    private AutenticacaoControlador controlador;

    @Test
    void cadastroRetornaCriado() {
        CadastroFuncionarioDTO dados = dados();
        Funcionario funcionario = new Funcionario();
        when(cadastro.cadastrar(dados)).thenReturn(funcionario);

        assertEquals(HttpStatus.CREATED, controlador.cadastrar(dados).getStatusCode());
    }

    @Test
    void cadastroDuplicadoRetornaConflito() {
        CadastroFuncionarioDTO dados = dados();
        when(cadastro.cadastrar(dados)).thenThrow(new IllegalArgumentException("Duplicada"));

        assertEquals(HttpStatus.CONFLICT, controlador.cadastrar(dados).getStatusCode());
    }

    @Test
    void loginRetornaToken() {
        LoginDTO dados = new LoginDTO("70", "senha-ficticia");
        LoginRespostaDTO token = new LoginRespostaDTO("token-teste");
        when(autenticacao.login(dados)).thenReturn(token);

        assertSame(token, controlador.login(dados).getBody());
    }

    private CadastroFuncionarioDTO dados() {
        return new CadastroFuncionarioDTO("João", "70", "Gerente",
                new BigDecimal("6000.00"), null, false, null);
    }
}
