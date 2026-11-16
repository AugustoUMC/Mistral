package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.FuncionarioRelatorioDTO;
import com.tcc.sistema_producao.dto.SomaSalariosDTO;
import com.tcc.sistema_producao.entidade.AlocacaoFuncionarioGrupo;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.repositorio.AlocacaoFuncionarioGrupoRepositorio;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class RelatorioFuncionarioServico {

    private final FuncionarioRepositorio funcionarioRepositorio;
    private final AlocacaoFuncionarioGrupoRepositorio alocacaoRepositorio;

    public RelatorioFuncionarioServico(
            FuncionarioRepositorio funcionarioRepositorio,
            AlocacaoFuncionarioGrupoRepositorio alocacaoRepositorio) {

        this.funcionarioRepositorio = funcionarioRepositorio;
        this.alocacaoRepositorio = alocacaoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<FuncionarioRelatorioDTO> listarFuncionarios() {

        List<Funcionario> funcionarios =
                funcionarioRepositorio.findAllByOrderByNomeAsc();

        if (funcionarios.isEmpty()) {
            return List.of();
        }

        List<Long> idsFuncionarios = funcionarios.stream()
                .map(Funcionario::getId)
                .toList();

        Map<Long, List<String>> gruposPorFuncionario =
                new HashMap<>();

        List<AlocacaoFuncionarioGrupo> alocacoesAtuais =
                alocacaoRepositorio
                        .findAllByFuncionario_IdInAndDataFimIsNullOrderByDataInicioAsc(
                                idsFuncionarios
                        );

        for (AlocacaoFuncionarioGrupo alocacao : alocacoesAtuais) {

            gruposPorFuncionario
                    .computeIfAbsent(
                            alocacao.getFuncionario().getId(),
                            id -> new ArrayList<>()
                    )
                    .add(alocacao.getGrupoProducao().getNome());
        }

        return funcionarios.stream()
                .map(funcionario -> converterParaDTO(
                        funcionario,
                        gruposPorFuncionario.getOrDefault(
                                funcionario.getId(),
                                List.of()
                        )
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public SomaSalariosDTO obterSomaSalarios() {

        BigDecimal soma = funcionarioRepositorio.somarSalarios();

        if (soma == null) {
            soma = BigDecimal.ZERO;
        }

        long quantidade = funcionarioRepositorio.count();

        return new SomaSalariosDTO(quantidade, soma);
    }

    @Transactional(readOnly = true)
    public SomaSalariosDTO obterSomaSalariosPorMatriculas(
            List<String> matriculas) {

        if (matriculas == null || matriculas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe pelo menos uma matrícula"
            );
        }

        List<String> matriculasNormalizadas = matriculas.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(matricula -> !matricula.isEmpty())
                .distinct()
                .toList();

        if (matriculasNormalizadas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe pelo menos uma matrícula válida"
            );
        }

        BigDecimal soma =
                funcionarioRepositorio.somarSalariosPorMatriculas(
                        matriculasNormalizadas
                );

        if (soma == null) {
            soma = BigDecimal.ZERO;
        }

        long quantidade =
                funcionarioRepositorio.countByMatriculaIn(
                        matriculasNormalizadas
                );

        return new SomaSalariosDTO(quantidade, soma);
    }

    private FuncionarioRelatorioDTO converterParaDTO(
            Funcionario funcionario,
            List<String> gruposAtuais) {

        return new FuncionarioRelatorioDTO(
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getMatricula(),
                funcionario.getCargo(),
                funcionario.getSalario(),
                funcionario.getAtivo(),
                funcionario.getAcessoSistema(),
                funcionario.getPerfil(),
                List.copyOf(gruposAtuais)
        );
    }
}