package com.tcc.sistema_producao.repositorio;

import com.tcc.sistema_producao.entidade.MetaProducao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaProducaoRepositorio extends JpaRepository<MetaProducao, Long> {

    List<MetaProducao> findByGrupoProducaoId(Long grupoProducaoId);

    List<MetaProducao> findByProdutoId(Long produtoId);

    boolean existsByGrupoProducaoIdAndProdutoId(
            Long grupoProducaoId,
            Long produtoId
    );

    boolean existsByGrupoProducaoIdAndProdutoIdAndIdNot(
            Long grupoProducaoId,
            Long produtoId,
            Long id
    );
}