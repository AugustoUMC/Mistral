package com.tcc.sistema_producao.repositorio;

import com.tcc.sistema_producao.entidade.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProdutoRepositorio extends JpaRepository<Produto, Long> {

    Optional<Produto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}