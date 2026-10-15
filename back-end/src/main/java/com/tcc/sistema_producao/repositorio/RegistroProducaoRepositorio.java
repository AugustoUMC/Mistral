
package com.tcc.sistema_producao.repositorio;

import com.tcc.sistema_producao.entidade.RegistroProducao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RegistroProducaoRepositorio
        extends JpaRepository<RegistroProducao, Long> {

    List<RegistroProducao> findAllByOrderByDataProducaoDesc();

    List<RegistroProducao>
    findByGrupoProducao_IdAndDataProducaoBetweenOrderByDataProducaoDesc(
            Long grupoId,
            LocalDate dataInicial,
            LocalDate dataFinal
    );

    List<RegistroProducao>
    findByDataProducaoBetweenOrderByDataProducaoDesc(
            LocalDate dataInicial,
            LocalDate dataFinal
    );
}
