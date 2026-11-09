package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.AlocacaoFuncionarioGrupoDTO;
import com.tcc.sistema_producao.entidade.AlocacaoFuncionarioGrupo;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.repositorio.AlocacaoFuncionarioGrupoRepositorio;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import com.tcc.sistema_producao.repositorio.GrupoProducaoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AlocacaoFuncionarioGrupoServico {

    private static final ZoneId FUSO_BRASIL =
            ZoneId.of("America/Sao_Paulo");

    private final FuncionarioRepositorio funcionarioRepositorio;
    private final GrupoProducaoRepositorio grupoProducaoRepositorio;
    private final AlocacaoFuncionarioGrupoRepositorio alocacaoRepositorio;
    private final AuditoriaServico auditoriaServico;

    public AlocacaoFuncionarioGrupoServico(
            FuncionarioRepositorio funcionarioRepositorio,
            GrupoProducaoRepositorio grupoProducaoRepositorio,
            AlocacaoFuncionarioGrupoRepositorio alocacaoRepositorio,
            AuditoriaServico auditoriaServico) {

        this.funcionarioRepositorio = funcionarioRepositorio;
        this.grupoProducaoRepositorio = grupoProducaoRepositorio;
        this.alocacaoRepositorio = alocacaoRepositorio;
        this.auditoriaServico = auditoriaServico;
    }

    @Transactional
    public List<AlocacaoFuncionarioGrupoDTO> atualizarGrupos(
            Long funcionarioId,
            List<Long> grupoIds) {

        if (grupoIds == null) {
            throw new IllegalArgumentException(
                    "Informe a lista de grupos."
            );
        }

        Funcionario funcionario = funcionarioRepositorio
                .findById(funcionarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Funcionário não encontrado."
                ));

        List<Long> idsSolicitados = new ArrayList<>();
        Set<Long> idsUnicos = new HashSet<>();

        for (Long grupoId : grupoIds) {

            if (grupoId == null || grupoId <= 0) {
                throw new IllegalArgumentException(
                        "Todos os identificadores de grupo devem ser válidos."
                );
            }

            if (!idsUnicos.add(grupoId)) {
                throw new IllegalArgumentException(
                        "A lista contém grupos repetidos."
                );
            }

            idsSolicitados.add(grupoId);
        }

        List<AlocacaoFuncionarioGrupo> alocacoesAtuais =
                alocacaoRepositorio
                        .findAllByFuncionario_IdAndDataFimIsNullOrderByDataInicioAsc(
                                funcionarioId
                        );

        Map<Long, AlocacaoFuncionarioGrupo> atuaisPorGrupo =
                new HashMap<>();

        for (AlocacaoFuncionarioGrupo alocacao : alocacoesAtuais) {
            atuaisPorGrupo.put(
                    alocacao.getGrupoProducao().getId(),
                    alocacao
            );
        }

        Set<Long> idsAtuais = atuaisPorGrupo.keySet();

        if (!Boolean.TRUE.equals(funcionario.getAtivo())
                && !idsAtuais.containsAll(idsUnicos)) {

            throw new IllegalArgumentException(
                    "Não é permitido adicionar grupos a um funcionário inativo."
            );
        }

        List<GrupoProducao> gruposSolicitados =
                idsSolicitados.isEmpty()
                        ? List.of()
                        : grupoProducaoRepositorio.findAllById(idsSolicitados);

        if (gruposSolicitados.size() != idsSolicitados.size()) {
            throw new IllegalArgumentException(
                    "Um ou mais grupos informados não foram encontrados."
            );
        }

        Map<Long, GrupoProducao> gruposPorId =
                gruposSolicitados.stream()
                        .collect(Collectors.toMap(
                                GrupoProducao::getId,
                                grupo -> grupo
                        ));

        for (GrupoProducao grupo : gruposSolicitados) {

            boolean jaEstavaAssociado =
                    idsAtuais.contains(grupo.getId());

            if (!Boolean.TRUE.equals(grupo.getAtivo())
                    && !jaEstavaAssociado) {

                throw new IllegalArgumentException(
                        "O grupo " + grupo.getNome()
                                + " está inativo e não pode receber novas associações."
                );
            }
        }

        LocalDate hoje = LocalDate.now(FUSO_BRASIL);

        int quantidadeAdicionada = 0;
        int quantidadeEncerrada = 0;

        for (AlocacaoFuncionarioGrupo alocacao : alocacoesAtuais) {

            Long grupoId = alocacao.getGrupoProducao().getId();

            if (!idsUnicos.contains(grupoId)) {
                alocacao.setDataFim(hoje);
                alocacaoRepositorio.save(alocacao);
                quantidadeEncerrada++;
            }
        }


        for (Long grupoId : idsSolicitados) {

            if (!idsAtuais.contains(grupoId)) {

                GrupoProducao grupo = gruposPorId.get(grupoId);

                AlocacaoFuncionarioGrupo novaAlocacao =
                        new AlocacaoFuncionarioGrupo();

                novaAlocacao.setFuncionario(funcionario);
                novaAlocacao.setGrupoProducao(grupo);
                novaAlocacao.setDataInicio(hoje);
                novaAlocacao.setDataFim(null);

                alocacaoRepositorio.save(novaAlocacao);
                quantidadeAdicionada++;
            }
        }

        if (quantidadeAdicionada > 0 || quantidadeEncerrada > 0) {

            auditoriaServico.registrarUsuarioAtual(
                    "ALTERACAO_GRUPOS_FUNCIONARIO",
                    "Associações do funcionário "
                            + funcionario.getMatricula()
                            + " atualizadas. Inclusões: "
                            + quantidadeAdicionada
                            + "; encerramentos: "
                            + quantidadeEncerrada
                            + "."
            );
        }

        return listarHistorico(funcionarioId);
    }

    @Transactional
    public int encerrarAssociacoesAtuais(Long funcionarioId) {

        List<AlocacaoFuncionarioGrupo> alocacoesAtuais =
                alocacaoRepositorio
                        .findAllByFuncionario_IdAndDataFimIsNullOrderByDataInicioAsc(
                                funcionarioId
                        );

        if (alocacoesAtuais.isEmpty()) {
            return 0;
        }

        LocalDate hoje = LocalDate.now(FUSO_BRASIL);

        for (AlocacaoFuncionarioGrupo alocacao : alocacoesAtuais) {
            alocacao.setDataFim(hoje);
        }

        alocacaoRepositorio.saveAll(alocacoesAtuais);

        return alocacoesAtuais.size();
    }

    @Transactional(readOnly = true)
    public List<AlocacaoFuncionarioGrupoDTO> listarHistorico(
            Long funcionarioId) {

        if (!funcionarioRepositorio.existsById(funcionarioId)) {
            throw new IllegalArgumentException(
                    "Funcionário não encontrado."
            );
        }

        return alocacaoRepositorio
                .findAllByFuncionario_IdOrderByDataInicioDesc(funcionarioId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private AlocacaoFuncionarioGrupoDTO converterParaDTO(
            AlocacaoFuncionarioGrupo alocacao) {

        return new AlocacaoFuncionarioGrupoDTO(
                alocacao.getId(),
                alocacao.getGrupoProducao().getId(),
                alocacao.getGrupoProducao().getNome(),
                alocacao.getDataInicio(),
                alocacao.getDataFim(),
                alocacao.getDataFim() == null
        );
    }
}