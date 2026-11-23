package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.dto.AtualizarGruposFuncionarioDTO;
import com.tcc.sistema_producao.dto.EnderecoFuncionarioDTO;
import com.tcc.sistema_producao.servico.AlocacaoFuncionarioGrupoServico;
import com.tcc.sistema_producao.servico.FuncionarioAdministracaoServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioControladorTest {

    @Mock
    private FuncionarioAdministracaoServico funcionarios;

    @Mock
    private AlocacaoFuncionarioGrupoServico alocacoes;

    @InjectMocks
    private FuncionarioControlador controlador;

    @Test
    void inativarRetornaSemConteudo() {
        assertEquals(HttpStatus.NO_CONTENT, controlador.inativar(1L).getStatusCode());
        verify(funcionarios).inativar(1L);
    }

    @Test
    void inativarFuncionarioInexistente() {
        doThrow(new IllegalArgumentException("Ausente")).when(funcionarios).inativar(1L);

        assertEquals(Map.of("erro", "Ausente"), controlador.inativar(1L).getBody());
        assertEquals(HttpStatus.NOT_FOUND, controlador.inativar(1L).getStatusCode());
    }

    @Test
    void consultarEnderecoSucessoEErro() {
        EnderecoFuncionarioDTO endereco = endereco();
        when(funcionarios.consultarEndereco(1L)).thenReturn(endereco)
                .thenThrow(new IllegalArgumentException("Ausente"));

        assertSame(endereco, controlador.consultarEndereco(1L).getBody());
        assertEquals(HttpStatus.NOT_FOUND, controlador.consultarEndereco(1L).getStatusCode());
    }

    @Test
    void atualizarEnderecoSucessoEErro() {
        EnderecoFuncionarioDTO endereco = endereco();
        when(funcionarios.atualizarEndereco(1L, endereco)).thenReturn(endereco)
                .thenThrow(new IllegalArgumentException("Ausente"));

        assertSame(endereco, controlador.atualizarEndereco(1L, endereco).getBody());
        assertEquals(HttpStatus.NOT_FOUND, controlador.atualizarEndereco(1L, endereco).getStatusCode());
    }

    @Test
    void listarAlocacoesSucessoEErro() {
        when(alocacoes.listarHistorico(1L)).thenReturn(List.of())
                .thenThrow(new IllegalArgumentException("Ausente"));

        assertEquals(HttpStatus.OK, controlador.listarAlocacoes(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controlador.listarAlocacoes(1L).getStatusCode());
    }

    @Test
    void atualizarGruposSucessoEErro() {
        AtualizarGruposFuncionarioDTO dados = new AtualizarGruposFuncionarioDTO(List.of(2L));
        when(alocacoes.atualizarGrupos(1L, List.of(2L))).thenReturn(List.of())
                .thenThrow(new IllegalArgumentException("Inválido"));

        assertEquals(HttpStatus.OK, controlador.atualizarGrupos(1L, dados).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controlador.atualizarGrupos(1L, dados).getStatusCode());
    }

    private EnderecoFuncionarioDTO endereco() {
        return new EnderecoFuncionarioDTO("08710-100", "Rua A", "1", null, "Centro", "Mogi", "SP");
    }
}
