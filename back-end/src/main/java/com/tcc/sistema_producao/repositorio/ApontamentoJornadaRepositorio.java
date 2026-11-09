package com.tcc.sistema_producao.repositorio;

import com.tcc.sistema_producao.entidade.ApontamentoJornada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ApontamentoJornadaRepositorio
        extends JpaRepository<ApontamentoJornada, Long> {

    List<ApontamentoJornada>
    findAllByFuncionario_IdAndInicioEmLessThanAndFimEmGreaterThan(
            Long funcionarioId,
            LocalDateTime novoFim,
            LocalDateTime novoInicio
    );

    @Query("""
            SELECT j
            FROM ApontamentoJornada j
            JOIN FETCH j.funcionario f
            JOIN FETCH j.grupoProducao g
            WHERE (:funcionarioId IS NULL OR f.id = :funcionarioId)
              AND (:grupoId IS NULL OR g.id = :grupoId)
              AND (:inicioPesquisa IS NULL OR j.inicioEm >= :inicioPesquisa)
              AND (:fimPesquisa IS NULL OR j.inicioEm < :fimPesquisa)
            ORDER BY j.inicioEm DESC, f.nome ASC
            """)
    List<ApontamentoJornada> pesquisar(
            @Param("funcionarioId") Long funcionarioId,
            @Param("grupoId") Long grupoId,
            @Param("inicioPesquisa") LocalDateTime inicioPesquisa,
            @Param("fimPesquisa") LocalDateTime fimPesquisa
    );
}