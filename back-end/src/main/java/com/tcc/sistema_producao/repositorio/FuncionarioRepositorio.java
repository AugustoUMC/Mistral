
package com.tcc.sistema_producao.repositorio;

import com.tcc.sistema_producao.entidade.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FuncionarioRepositorio
        extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByMatricula(String matricula);

    Optional<Funcionario> findByMatriculaAndAtivoTrue(String matricula);

    List<Funcionario> findAllByOrderByNomeAsc();

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
