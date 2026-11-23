package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.repositorio.GrupoProducaoRepositorio;
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
class GrupoProducaoServicoTest {

    @Mock
    private GrupoProducaoRepositorio repositorio;

    @Mock
    private AuditoriaServico auditoriaServico;

    @InjectMocks
    private GrupoProducaoServico servico;

    @Test
    void cadastrarAtivaGrupoERegistraAuditoria() {
        GrupoProducao grupo = new GrupoProducao();
        grupo.setNome("Grupo A");
        grupo.setAtivo(false);

        when(repositorio.save(grupo)).thenReturn(grupo);

        GrupoProducao resultado = servico.cadastrar(grupo);

        assertSame(grupo, resultado);
        assertTrue(resultado.getAtivo());
        verify(repositorio).save(grupo);
        verify(auditoriaServico).registrarUsuarioAtual(
                "CADASTRO_GRUPO", "Grupo Grupo A cadastrado");
    }

    @Test
    void cadastrarRejeitaNomeDuplicado() {
        GrupoProducao grupo = new GrupoProducao();
        grupo.setNome("Grupo A");
        when(repositorio.existsByNome("Grupo A")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> servico.cadastrar(grupo));

        verify(repositorio, never()).save(any());
        verifyNoInteractions(auditoriaServico);
    }

    @Test
    void listarRetornaGruposDoRepositorio() {
        GrupoProducao grupo = new GrupoProducao();
        grupo.setNome("Grupo B");
        when(repositorio.findAll()).thenReturn(List.of(grupo));

        assertEquals(List.of(grupo), servico.listar());
    }

    @Test
    void buscarPorIdRetornaGrupoExistente() {
        GrupoProducao grupo = new GrupoProducao();
        when(repositorio.findById(8L)).thenReturn(Optional.of(grupo));

        assertSame(grupo, servico.buscarPorId(8L));
    }

    @Test
    void buscarPorIdRejeitaGrupoInexistente() {
        when(repositorio.findById(8L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> servico.buscarPorId(8L));
    }

    @Test
    void atualizarAlteraNomeStatusERegistraAuditoria() {
        GrupoProducao salvo = new GrupoProducao();
        salvo.setNome("Grupo Antigo");
        salvo.setAtivo(true);

        GrupoProducao dados = new GrupoProducao();
        dados.setNome("Grupo Novo");
        dados.setAtivo(false);

        when(repositorio.findById(1L)).thenReturn(Optional.of(salvo));
        when(repositorio.save(salvo)).thenReturn(salvo);

        GrupoProducao atualizado = servico.atualizar(1L, dados);

        assertSame(salvo, atualizado);
        assertEquals("Grupo Novo", atualizado.getNome());
        assertFalse(atualizado.getAtivo());
        verify(auditoriaServico).registrarUsuarioAtual(
                "ALTERACAO_GRUPO", "Grupo Grupo Antigo alterado para Grupo Novo");
    }

    @Test
    void atualizarPermiteManterMesmoNome() {
        GrupoProducao salvo = new GrupoProducao();
        salvo.setNome("Grupo A");
        GrupoProducao dados = new GrupoProducao();
        dados.setNome("Grupo A");

        when(repositorio.findById(1L)).thenReturn(Optional.of(salvo));
        when(repositorio.save(salvo)).thenReturn(salvo);

        servico.atualizar(1L, dados);

        verify(repositorio, never()).existsByNome(any());
        verify(repositorio).save(salvo);
    }

    @Test
    void atualizarRejeitaNomeUtilizadoPorOutroGrupo() {
        GrupoProducao salvo = new GrupoProducao();
        salvo.setNome("Grupo A");
        GrupoProducao dados = new GrupoProducao();
        dados.setNome("Grupo B");

        when(repositorio.findById(1L)).thenReturn(Optional.of(salvo));
        when(repositorio.existsByNome("Grupo B")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> servico.atualizar(1L, dados));

        verify(repositorio, never()).save(any());
    }

    @Test
    void excluirRemoveGrupoERegistraAuditoria() {
        GrupoProducao grupo = new GrupoProducao();
        grupo.setNome("Grupo A");
        when(repositorio.findById(1L)).thenReturn(Optional.of(grupo));

        servico.excluir(1L);

        verify(repositorio).delete(grupo);
        verify(auditoriaServico).registrarUsuarioAtual(
                "EXCLUSAO_GRUPO", "Grupo Grupo A excluído");
    }
}
