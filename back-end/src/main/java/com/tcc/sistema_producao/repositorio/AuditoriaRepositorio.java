package com.tcc.sistema_producao.repositorio;

import com.tcc.sistema_producao.entidade.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepositorio extends JpaRepository<Auditoria, Long> {
}