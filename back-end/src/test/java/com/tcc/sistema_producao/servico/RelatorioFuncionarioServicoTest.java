package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.FuncionarioRelatorioDTO;
import com.tcc.sistema_producao.dto.SomaSalariosDTO;
import com.tcc.sistema_producao.entidade.AlocacaoFuncionarioGrupo;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.repositorio.AlocacaoFuncionarioGrupoRepositorio;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioFuncionarioServicoTest {

    @Mock
    private FuncionarioRepositorio funcionarios;

    @Mock
    private AlocacaoFuncionarioGrupoRepositorio alocacoes;

    @InjectMocks
    private RelatorioFuncionarioServico servico;

    @Test
    void listaVaziaNaoConsultaVinculos() {
        when(funcionarios.findAllByOrderByNomeAsc()).thenReturn(List.of());

        assertTrue(servico.listarFuncionarios().isEmpty());
        verifyNoInteractions(alocacoes);
    }

    @Test
    void listarFuncionariosIncluiGruposAtuais() {
        Funcionario primeiro = funcionario(1L, "Isabela", "9");
        Funcionario segundo = funcionario(2L, "João", "70");
        when(funcionarios.findAllByOrderByNomeAsc()).thenReturn(List.of(primeiro, segundo));
        when(alocacoes.findAllByFuncionario_IdInAndDataFimIsNullOrderByDataInicioAsc(List.of(1L, 2L)))
                .thenReturn(List.of(alocacao(primeiro, "Grupo A"), alocacao(primeiro, "Grupo B")));

        List<FuncionarioRelatorioDTO> resposta = servico.listarFuncionarios();

        assertEquals(2, resposta.size());
        assertEquals("Isabela", resposta.get(0).nome());
        assertEquals(List.of("Grupo A", "Grupo B"), resposta.get(0).gruposAtuais());
        assertEquals("João", resposta.get(1).nome());
        assertTrue(resposta.get(1).gruposAtuais().isEmpty());
    }

    @Test
    void somaSalariosCalculaTotal() {
        when(funcionarios.somarSalarios()).thenReturn(new BigDecimal("8250.00"));
        when(funcionarios.count()).thenReturn(2L);

        SomaSalariosDTO total = servico.obterSomaSalarios();

        assertEquals(2L, total.quantidadeFuncionarios());
        assertEquals(new BigDecimal("8250.00"), total.somaSalarios());
    }

    @Test
    void somaSalariosNulaRetornaZero() {
        SomaSalariosDTO total = servico.obterSomaSalarios();

        assertEquals(BigDecimal.ZERO, total.somaSalarios());
        assertEquals(0L, total.quantidadeFuncionarios());
    }

    @Test
    void somaPorMatriculasRejeitaListasInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> servico.obterSomaSalariosPorMatriculas(null));
        assertThrows(IllegalArgumentException.class, () -> servico.obterSomaSalariosPorMatriculas(List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> servico.obterSomaSalariosPorMatriculas(java.util.Arrays.asList(null, "  ")));
        verifyNoInteractions(funcionarios);
    }

    @Test
    void somaPorMatriculasRemoveRepeticoesEBrancos() {
        List<String> matriculas = List.of("9", "70");
        when(funcionarios.somarSalariosPorMatriculas(matriculas))
                .thenReturn(new BigDecimal("8750.00"));
        when(funcionarios.countByMatriculaIn(matriculas)).thenReturn(2L);

        SomaSalariosDTO total = servico.obterSomaSalariosPorMatriculas(
                java.util.Arrays.asList(" 9 ", "70", "9", "  ", null));

        assertEquals(2L, total.quantidadeFuncionarios());
        assertEquals(new BigDecimal("8750.00"), total.somaSalarios());
    }

    @Test
    void somaPorMatriculasSemSalarioRetornaZero() {
        when(funcionarios.countByMatriculaIn(List.of("9"))).thenReturn(1L);

        SomaSalariosDTO total = servico.obterSomaSalariosPorMatriculas(List.of("9"));

        assertEquals(BigDecimal.ZERO, total.somaSalarios());
        assertEquals(1L, total.quantidadeFuncionarios());
    }

    private Funcionario funcionario(Long id, String nome, String matricula) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(id);
        funcionario.setNome(nome);
        funcionario.setMatricula(matricula);
        funcionario.setCargo("Funcionário");
        funcionario.setSalario(new BigDecimal("2500.00"));
        funcionario.setAtivo(true);
        return funcionario;
    }

    private AlocacaoFuncionarioGrupo alocacao(Funcionario funcionario, String grupoNome) {
        GrupoProducao grupo = new GrupoProducao();
        grupo.setId(10L);
        grupo.setNome(grupoNome);
        AlocacaoFuncionarioGrupo alocacao = new AlocacaoFuncionarioGrupo();
        alocacao.setFuncionario(funcionario);
        alocacao.setGrupoProducao(grupo);
        alocacao.setDataInicio(LocalDate.of(2026, 11, 1));
        return alocacao;
    }
}
