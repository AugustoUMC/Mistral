
package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.dto.FuncionarioRelatorioDTO;
import com.tcc.sistema_producao.dto.SomaSalariosDTO;
import com.tcc.sistema_producao.servico.RelatorioFuncionarioServico;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/relatorios/funcionarios")
public class RelatorioFuncionarioControlador {

    private final RelatorioFuncionarioServico relatorioServico;

    public RelatorioFuncionarioControlador(
            RelatorioFuncionarioServico relatorioServico) {
        this.relatorioServico = relatorioServico;
    }

    @GetMapping
    public List<FuncionarioRelatorioDTO> listarFuncionarios() {
        return relatorioServico.listarFuncionarios();
    }

    @GetMapping("/soma-salarios")
    public SomaSalariosDTO obterSomaSalarios() {
        return relatorioServico.obterSomaSalarios();
    }

    @GetMapping("/soma-salarios/selecionados")
    public SomaSalariosDTO obterSomaSalariosSelecionados(
            @RequestParam List<String> matriculas) {

        return relatorioServico.obterSomaSalariosPorMatriculas(
                matriculas
        );
    }
}
