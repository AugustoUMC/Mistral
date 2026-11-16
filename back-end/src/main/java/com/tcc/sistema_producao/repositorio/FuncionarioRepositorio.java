package com.tcc.sistema_producao.repositorio;

import com.tcc.sistema_producao.entidade.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FuncionarioRepositorio
        extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByMatricula(String matricula);

    Optional<Funcionario> findByMatriculaAndAtivoTrue(String matricula);

    List<Funcionario> findAllByOrderByNomeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Funcionario f WHERE f.id = :id")
    Optional<Funcionario> findByIdComBloqueio(@Param("id") Long id);

    @Query("SELECT SUM(f.salario) FROM Funcionario f")
    BigDecimal somarSalarios();

    @Query("""
            SELECT SUM(f.salario)
            FROM Funcionario f
            WHERE f.matricula IN :matriculas
            """)
    BigDecimal somarSalariosPorMatriculas(
            @Param("matriculas") List<String> matriculas
    );

    long countByMatriculaIn(List<String> matriculas);
}