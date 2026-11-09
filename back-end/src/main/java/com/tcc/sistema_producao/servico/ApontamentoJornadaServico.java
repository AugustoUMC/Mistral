package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.ApontamentoJornadaDTO;
import com.tcc.sistema_producao.dto.ApontamentoJornadaRespostaDTO;
import com.tcc.sistema_producao.entidade.ApontamentoJornada;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.repositorio.AlocacaoFuncionarioGrupoRepositorio;
import com.tcc.sistema_producao.repositorio.ApontamentoJornadaRepositorio;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import com.tcc.sistema_producao.repositorio.GrupoProducaoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ApontamentoJornadaServico {

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ApontamentoJornadaRepositorio jornadaRepositorio;
    private final FuncionarioRepositorio funcionarioRepositorio;
    private final GrupoProducaoRepositorio grupoRepositorio;
    private final AlocacaoFuncionarioGrupoRepositorio alocacaoRepositorio;
    private final AuditoriaServico auditoriaServico;

    public ApontamentoJornadaServico(
            ApontamentoJornadaRepositorio jornadaRepositorio,
            FuncionarioRepositorio funcionarioRepositorio,
            GrupoProducaoRepositorio grupoRepositorio,
            AlocacaoFuncionarioGrupoRepositorio alocacaoRepositorio,
            AuditoriaServico auditoriaServico) {

        this.jornadaRepositorio = jornadaRepositorio;
        this.funcionarioRepositorio = funcionarioRepositorio;
        this.grupoRepositorio = grupoRepositorio;
        this.alocacaoRepositorio = alocacaoRepositorio;
        this.auditoriaServico = auditoriaServico;
    }

    @Transactional
    public ApontamentoJornadaRespostaDTO cadastrar(ApontamentoJornadaDTO dto) {

        DadosHorario horario = calcularHorario(dto);

        Funcionario funcionario = obterFuncionarioComBloqueio(
                dto.funcionarioId()
        );

        GrupoProducao grupo = obterGrupo(dto.grupoProducaoId());

        validarAlocacaoDurantePeriodo(
                funcionario.getId(),
                grupo.getId(),
                horario.inicio().toLocalDate(),
                horario.fim().toLocalDate()
        );

        validarAusenciaDeSobreposicao(
                funcionario.getId(),
                horario.inicio(),
                horario.fim(),
                null
        );

        ApontamentoJornada apontamento = new ApontamentoJornada();

        apontamento.setFuncionario(funcionario);
        apontamento.setGrupoProducao(grupo);
        apontamento.setInicioEm(horario.inicio());
        apontamento.setFimEm(horario.fim());
        apontamento.setIntervaloMinutos(dto.intervaloMinutos());
        apontamento.setMinutosLiquidos(horario.minutosLiquidos());

        ApontamentoJornada salvo = jornadaRepositorio.save(apontamento);

        auditoriaServico.registrarUsuarioAtual(
                "CADASTRO_JORNADA",
                criarDescricaoAuditoria(salvo, "cadastrada")
        );

        return converterParaDTO(salvo);
    }

    @Transactional
    public ApontamentoJornadaRespostaDTO atualizar(
            Long id,
            ApontamentoJornadaDTO dto) {

        ApontamentoJornada existente = jornadaRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Apontamento de jornada não encontrado."
                ));

        if (!existente.getFuncionario().getId().equals(dto.funcionarioId())) {
            throw new IllegalArgumentException(
                    "O funcionário de um apontamento não pode ser substituído durante a edição."
            );
        }

        DadosHorario horario = calcularHorario(dto);

        Funcionario funcionario = obterFuncionarioComBloqueio(
                existente.getFuncionario().getId()
        );

        GrupoProducao grupo = obterGrupo(dto.grupoProducaoId());

        validarAlocacaoDurantePeriodo(
                funcionario.getId(),
                grupo.getId(),
                horario.inicio().toLocalDate(),
                horario.fim().toLocalDate()
        );

        validarAusenciaDeSobreposicao(
                funcionario.getId(),
                horario.inicio(),
                horario.fim(),
                id
        );

        String resumoAnterior = criarResumoAuditoria(existente);

        existente.setFuncionario(funcionario);
        existente.setGrupoProducao(grupo);
        existente.setInicioEm(horario.inicio());
        existente.setFimEm(horario.fim());
        existente.setIntervaloMinutos(dto.intervaloMinutos());
        existente.setMinutosLiquidos(horario.minutosLiquidos());

        ApontamentoJornada salvo = jornadaRepositorio.save(existente);

        auditoriaServico.registrarUsuarioAtual(
                "ALTERACAO_JORNADA",
                "Jornada #" + salvo.getId()
                        + " alterada. Antes: " + resumoAnterior
                        + ". Depois: " + criarResumoAuditoria(salvo) + "."
        );

        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<ApontamentoJornadaRespostaDTO> pesquisar(
            Long funcionarioId,
            Long grupoId,
            LocalDate dataInicial,
            LocalDate dataFinal) {

        if (dataInicial != null && dataFinal != null
                && dataInicial.isAfter(dataFinal)) {

            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final."
            );
        }

        LocalDateTime inicioPesquisa = dataInicial == null
                ? null
                : dataInicial.atStartOfDay();

        LocalDateTime fimPesquisa = dataFinal == null
                ? null
                : dataFinal.plusDays(1).atStartOfDay();

        return jornadaRepositorio.pesquisar(
                        funcionarioId,
                        grupoId,
                        inicioPesquisa,
                        fimPesquisa
                )
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private Funcionario obterFuncionarioComBloqueio(Long funcionarioId) {
        return funcionarioRepositorio.findByIdComBloqueio(funcionarioId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Funcionário não encontrado."
                ));
    }

    private GrupoProducao obterGrupo(Long grupoId) {
        return grupoRepositorio.findById(grupoId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Grupo de produção não encontrado."
                ));
    }

    private DadosHorario calcularHorario(ApontamentoJornadaDTO dto) {

        if (dto.dataJornada() == null
                || dto.horarioInicio() == null
                || dto.horarioFim() == null
                || dto.intervaloMinutos() == null) {

            throw new IllegalArgumentException(
                    "Informe data, início, término e duração dos intervalos."
            );
        }

        if (dto.intervaloMinutos() < 0) {
            throw new IllegalArgumentException(
                    "A duração dos intervalos não pode ser negativa."
            );
        }

        LocalTime inicioInformado = dto.horarioInicio();
        LocalTime fimInformado = dto.horarioFim();

        if (inicioInformado.equals(fimInformado)) {
            throw new IllegalArgumentException(
                    "Os horários de início e término não podem ser iguais."
            );
        }

        if (inicioInformado.getSecond() != 0
                || inicioInformado.getNano() != 0
                || fimInformado.getSecond() != 0
                || fimInformado.getNano() != 0) {

            throw new IllegalArgumentException(
                    "Os horários devem ser informados com precisão de minutos."
            );
        }

        LocalDateTime inicio = LocalDateTime.of(
                dto.dataJornada(),
                inicioInformado
        );

        LocalDate dataFim = fimInformado.isBefore(inicioInformado)
                ? dto.dataJornada().plusDays(1)
                : dto.dataJornada();

        LocalDateTime fim = LocalDateTime.of(dataFim, fimInformado);

        long minutosBrutos = Duration.between(inicio, fim).toMinutes();

        if (dto.intervaloMinutos() > minutosBrutos) {
            throw new IllegalArgumentException(
                    "A duração dos intervalos não pode superar o tempo total do apontamento."
            );
        }

        int minutosLiquidos = Math.toIntExact(
                minutosBrutos - dto.intervaloMinutos()
        );

        return new DadosHorario(
                inicio,
                fim,
                minutosLiquidos
        );
    }

    private void validarAlocacaoDurantePeriodo(
            Long funcionarioId,
            Long grupoId,
            LocalDate dataInicio,
            LocalDate dataFim) {

        boolean associadoNoInicio =
                alocacaoRepositorio.existeAssociacaoVigenteNaData(
                        funcionarioId,
                        grupoId,
                        dataInicio
                );

        boolean associadoNoFim =
                alocacaoRepositorio.existeAssociacaoVigenteNaData(
                        funcionarioId,
                        grupoId,
                        dataFim
                );

        if (!associadoNoInicio || !associadoNoFim) {
            throw new IllegalArgumentException(
                    "O funcionário não estava associado a esse grupo durante todo o período informado."
            );
        }
    }

    private void validarAusenciaDeSobreposicao(
            Long funcionarioId,
            LocalDateTime novoInicio,
            LocalDateTime novoFim,
            Long apontamentoIgnoradoId) {

        List<ApontamentoJornada> possiveisConflitos =
                jornadaRepositorio
                        .findAllByFuncionario_IdAndInicioEmLessThanAndFimEmGreaterThan(
                                funcionarioId,
                                novoFim,
                                novoInicio
                        );

        boolean existeConflito = possiveisConflitos.stream()
                .anyMatch(apontamento ->
                        apontamentoIgnoradoId == null
                                || !apontamento.getId().equals(apontamentoIgnoradoId)
                );

        if (existeConflito) {
            throw new IllegalStateException(
                    "Já existe um apontamento para esse funcionário com horários sobrepostos. Ajuste os horários ou consulte os registros existentes."
            );
        }
    }

    private ApontamentoJornadaRespostaDTO converterParaDTO(
            ApontamentoJornada apontamento) {

        long minutosBrutos = Duration.between(
                apontamento.getInicioEm(),
                apontamento.getFimEm()
        ).toMinutes();

        return new ApontamentoJornadaRespostaDTO(
                apontamento.getId(),
                apontamento.getFuncionario().getId(),
                apontamento.getFuncionario().getNome(),
                apontamento.getFuncionario().getMatricula(),
                apontamento.getGrupoProducao().getId(),
                apontamento.getGrupoProducao().getNome(),
                apontamento.getInicioEm().toLocalDate(),
                apontamento.getFimEm().toLocalDate(),
                apontamento.getInicioEm().toLocalTime(),
                apontamento.getFimEm().toLocalTime(),
                Math.toIntExact(minutosBrutos),
                apontamento.getIntervaloMinutos(),
                apontamento.getMinutosLiquidos()
        );
    }

    private String criarDescricaoAuditoria(
            ApontamentoJornada apontamento,
            String acao) {

        return "Jornada #" + apontamento.getId()
                + " " + acao
                + ". Funcionário: "
                + apontamento.getFuncionario().getMatricula()
                + "; " + criarResumoAuditoria(apontamento) + ".";
    }

    private String criarResumoAuditoria(ApontamentoJornada apontamento) {
        return "grupo " + apontamento.getGrupoProducao().getId()
                + ", início " + apontamento.getInicioEm().format(FORMATO_DATA_HORA)
                + ", término " + apontamento.getFimEm().format(FORMATO_DATA_HORA)
                + ", intervalo " + apontamento.getIntervaloMinutos()
                + " min, líquido " + apontamento.getMinutosLiquidos() + " min";
    }

    private record DadosHorario(
            LocalDateTime inicio,
            LocalDateTime fim,
            Integer minutosLiquidos
    ) {
    }
}