package com.tcc.sistema_producao.repositorio;

import com.tcc.sistema_producao.entidade.GrupoProducao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrupoProducaoRepositorio extends JpaRepository<GrupoProducao, Long> {

    Optional<GrupoProducao> findByNome(String nome);

    boolean existsByNome(String nome);
}