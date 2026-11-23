package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.AlocacaoFuncionarioGrupoDTO;
import com.tcc.sistema_producao.entidade.AlocacaoFuncionarioGrupo;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.repositorio.AlocacaoFuncionarioGrupoRepositorio;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import com.tcc.sistema_producao.repositorio.GrupoProducaoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlocacaoFuncionarioGrupoServicoTest {

    @Mock
    private FuncionarioRepositorio funcionarios;

    @Mock
    private GrupoProducaoRepositorio grupos;

    @Mock
    private AlocacaoFuncionarioGrupoRepositorio alocacoes;

    @Mock
    private AuditoriaServico auditoria;

    @InjectMocks
    private AlocacaoFuncionarioGrupoServico servico;

    @Test
    void rejeitaListaNula() {
        assertThrows(IllegalArgumentException.class, () -> servico.atualizarGrupos(1L, null));
        verifyNoInteractions(funcionarios, grupos, alocacoes, auditoria);
    }

    @Test
    void rejeitaFuncionarioInexistente() {
        when(funcionarios.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servico.atualizarGrupos(1L, List.of(2L)));
    }

    @Test
    void rejeitaIdentificadorDeGrupoInvalido() {
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario(true)));

        assertThrows(IllegalArgumentException.class, () -> servico.atualizarGrupos(1L, List.of(0L)));
        assertThrows(IllegalArgumentException.class, () -> servico.atualizarGrupos(1L, List.of(-1L)));
        assertThrows(IllegalArgumentException.class, () -> servico.atualizarGrupos(1L,
                java.util.Arrays.asList((Long) null)));
        verifyNoInteractions(alocacoes, grupos);
    }

    @Test
    void rejeitaGrupoRepetido() {
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario(true)));

        assertThrows(IllegalArgumentException.class,
                () -> servico.atualizarGrupos(1L, List.of(2L, 2L)));
        verifyNoInteractions(alocacoes, grupos);
    }

    @Test
    void rejeitaGrupoNaoEncontrado() {
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario(true)));
        when(grupos.findAllById(List.of(2L))).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> servico.atualizarGrupos(1L, List.of(2L)));
        verify(alocacoes, never()).save(any());
    }

    @Test
    void rejeitaGrupoInativoParaNovoVinculo() {
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario(true)));
        when(grupos.findAllById(List.of(2L))).thenReturn(List.of(grupo(2L, false)));

        assertThrows(IllegalArgumentException.class,
                () -> servico.atualizarGrupos(1L, List.of(2L)));
        verify(alocacoes, never()).save(any());
    }

    @Test
    void rejeitaNovoGrupoParaFuncionarioInativo() {
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario(false)));

        assertThrows(IllegalArgumentException.class,
                () -> servico.atualizarGrupos(1L, List.of(2L)));
        verifyNoInteractions(grupos);
    }

    @Test
    void adicionaAssociacaoNovaEAudita() {
        Funcionario funcionario = funcionario(true);
        GrupoProducao grupo = grupo(2L, true);
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario));
        when(funcionarios.existsById(1L)).thenReturn(true);
        when(grupos.findAllById(List.of(2L))).thenReturn(List.of(grupo));
        when(alocacoes.findAllByFuncionario_IdOrderByDataInicioDesc(1L)).thenReturn(List.of());

        assertTrue(servico.atualizarGrupos(1L, List.of(2L)).isEmpty());

        ArgumentCaptor<AlocacaoFuncionarioGrupo> captor =
                ArgumentCaptor.forClass(AlocacaoFuncionarioGrupo.class);
        verify(alocacoes).save(captor.capture());
        assertSame(funcionario, captor.getValue().getFuncionario());
        assertSame(grupo, captor.getValue().getGrupoProducao());
        assertNotNull(captor.getValue().getDataInicio());
        assertNull(captor.getValue().getDataFim());
        verify(auditoria).registrarUsuarioAtual(
                "ALTERACAO_GRUPOS_FUNCIONARIO",
                "Associações do funcionário 70 atualizadas. Inclusões: 1; encerramentos: 0.");
    }

    @Test
    void removerAssociacaoExistenteEncerraHistorico() {
        AlocacaoFuncionarioGrupo anterior = alocacao(grupo(2L, true));
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario(true)));
        when(funcionarios.existsById(1L)).thenReturn(true);
        when(alocacoes.findAllByFuncionario_IdAndDataFimIsNullOrderByDataInicioAsc(1L))
                .thenReturn(List.of(anterior));
        when(alocacoes.findAllByFuncionario_IdOrderByDataInicioDesc(1L)).thenReturn(List.of(anterior));

        List<AlocacaoFuncionarioGrupoDTO> historico = servico.atualizarGrupos(1L, List.of());

        assertNotNull(anterior.getDataFim());
        assertFalse(historico.get(0).atual());
        verify(alocacoes).save(anterior);
        verify(auditoria).registrarUsuarioAtual(
                "ALTERACAO_GRUPOS_FUNCIONARIO",
                "Associações do funcionário 70 atualizadas. Inclusões: 0; encerramentos: 1.");
    }

    @Test
    void manterAssociacaoNaoGeraNovaAuditoria() {
        GrupoProducao grupo = grupo(2L, true);
        AlocacaoFuncionarioGrupo anterior = alocacao(grupo);
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario(true)));
        when(funcionarios.existsById(1L)).thenReturn(true);
        when(alocacoes.findAllByFuncionario_IdAndDataFimIsNullOrderByDataInicioAsc(1L))
                .thenReturn(List.of(anterior));
        when(alocacoes.findAllByFuncionario_IdOrderByDataInicioDesc(1L)).thenReturn(List.of(anterior));
        when(grupos.findAllById(List.of(2L))).thenReturn(List.of(grupo));

        List<AlocacaoFuncionarioGrupoDTO> historico = servico.atualizarGrupos(1L, List.of(2L));

        assertTrue(historico.get(0).atual());
        verify(alocacoes, never()).save(any());
        verifyNoInteractions(auditoria);
    }

    @Test
    void funcionarioInativoPodeManterGrupoJaAssociado() {
        GrupoProducao grupo = grupo(2L, false);
        when(funcionarios.findById(1L)).thenReturn(Optional.of(funcionario(false)));
        when(funcionarios.existsById(1L)).thenReturn(true);
        when(alocacoes.findAllByFuncionario_IdAndDataFimIsNullOrderByDataInicioAsc(1L))
                .thenReturn(List.of(alocacao(grupo)));
        when(alocacoes.findAllByFuncionario_IdOrderByDataInicioDesc(1L)).thenReturn(List.of());
        when(grupos.findAllById(List.of(2L))).thenReturn(List.of(grupo));

        assertDoesNotThrow(() -> servico.atualizarGrupos(1L, List.of(2L)));
        verifyNoInteractions(auditoria);
    }

    @Test
    void encerrarSemAssociacoesRetornaZero() {
        assertEquals(0, servico.encerrarAssociacoesAtuais(1L));
        verify(alocacoes, never()).saveAll(any());
    }

    @Test
    void encerrarAssociacoesRegistraFimDasDuas() {
        AlocacaoFuncionarioGrupo primeira = alocacao(grupo(2L, true));
        AlocacaoFuncionarioGrupo segunda = alocacao(grupo(3L, true));
        when(alocacoes.findAllByFuncionario_IdAndDataFimIsNullOrderByDataInicioAsc(1L))
                .thenReturn(List.of(primeira, segunda));

        assertEquals(2, servico.encerrarAssociacoesAtuais(1L));

        assertNotNull(primeira.getDataFim());
        assertEquals(primeira.getDataFim(), segunda.getDataFim());
        verify(alocacoes).saveAll(List.of(primeira, segunda));
    }

    @Test
    void historicoRejeitaFuncionarioInexistente() {
        when(funcionarios.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> servico.listarHistorico(1L));
    }

    @Test
    void historicoConverteVinculosEncerrados() {
        AlocacaoFuncionarioGrupo vinculo = alocacao(grupo(2L, true));
        vinculo.setDataFim(LocalDate.of(2026, 11, 1));
        when(funcionarios.existsById(1L)).thenReturn(true);
        when(alocacoes.findAllByFuncionario_IdOrderByDataInicioDesc(1L))
                .thenReturn(List.of(vinculo));

        List<AlocacaoFuncionarioGrupoDTO> resposta = servico.listarHistorico(1L);

        assertEquals(1, resposta.size());
        assertEquals(2L, resposta.get(0).grupoId());
        assertEquals("Grupo 2", resposta.get(0).grupoNome());
        assertFalse(resposta.get(0).atual());
    }

    private Funcionario funcionario(boolean ativo) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setMatricula("70");
        funcionario.setAtivo(ativo);
        return funcionario;
    }

    private GrupoProducao grupo(Long id, boolean ativo) {
        GrupoProducao grupo = new GrupoProducao();
        grupo.setId(id);
        grupo.setNome("Grupo " + id);
        grupo.setAtivo(ativo);
        return grupo;
    }

    private AlocacaoFuncionarioGrupo alocacao(GrupoProducao grupo) {
        AlocacaoFuncionarioGrupo vinculo = new AlocacaoFuncionarioGrupo();
        vinculo.setId(5L);
        vinculo.setGrupoProducao(grupo);
        vinculo.setFuncionario(funcionario(true));
        vinculo.setDataInicio(LocalDate.of(2026, 10, 1));
        return vinculo;
    }
}
