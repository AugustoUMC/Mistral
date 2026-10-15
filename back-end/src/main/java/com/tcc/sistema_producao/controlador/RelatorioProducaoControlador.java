
package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.dto.RelatorioProducaoDTO;
import com.tcc.sistema_producao.servico.RelatorioProducaoServico;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/relatorios/producao")
public class RelatorioProducaoControlador {

    private final RelatorioProducaoServico relatorioServico;

    public RelatorioProducaoControlador(
            RelatorioProducaoServico relatorioServico) {
        this.relatorioServico = relatorioServico;
    }

    @GetMapping("/global")
    public ResponseEntity<?> gerarRelatorioGlobal(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataInicial,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataFinal) {

        try {
            RelatorioProducaoDTO relatorio =
                    relatorioServico.gerarRelatorioGlobal(
                            dataInicial,
                            dataFinal
                    );

            return ResponseEntity.ok(relatorio);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/mensal")
    public ResponseEntity<?> gerarRelatorioMensal(
            @RequestParam int ano,
            @RequestParam int mes) {

        try {
            RelatorioProducaoDTO relatorio =
                    relatorioServico.gerarRelatorioMensal(
                            ano,
                            mes
                    );

            return ResponseEntity.ok(relatorio);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
