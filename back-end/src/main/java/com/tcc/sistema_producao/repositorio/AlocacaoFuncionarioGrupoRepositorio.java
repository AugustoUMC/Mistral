package com.tcc.sistema_producao.repositorio;

import com.tcc.sistema_producao.entidade.AlocacaoFuncionarioGrupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface AlocacaoFuncionarioGrupoRepositorio
        extends JpaRepository<AlocacaoFuncionarioGrupo, Long> {

    List<AlocacaoFuncionarioGrupo>
    findAllByFuncionario_IdAndDataFimIsNullOrderByDataInicioAsc(
            Long funcionarioId
    );

    List<AlocacaoFuncionarioGrupo>
    findAllByFuncionario_IdOrderByDataInicioDesc(
            Long funcionarioId
    );

    List<AlocacaoFuncionarioGrupo>
    findAllByFuncionario_IdInAndDataFimIsNullOrderByDataInicioAsc(
            Collection<Long> funcionarioIds
    );

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM AlocacaoFuncionarioGrupo a
            WHERE a.funcionario.id = :funcionarioId
              AND a.grupoProducao.id = :grupoId
              AND a.dataInicio <= :dataReferencia
              AND (a.dataFim IS NULL OR a.dataFim >= :dataReferencia)
            """)
    boolean existeAssociacaoVigenteNaData(
            @Param("funcionarioId") Long funcionarioId,
            @Param("grupoId") Long grupoId,
            @Param("dataReferencia") LocalDate dataReferencia
    );
}