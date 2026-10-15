
package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.dto.RegistroProducaoDTO;
import com.tcc.sistema_producao.dto.RegistroProducaoRespostaDTO;
import com.tcc.sistema_producao.servico.RegistroProducaoServico;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/registros-producao")
public class RegistroProducaoControlador {

    private final RegistroProducaoServico registroServico;

    public RegistroProducaoControlador(
            RegistroProducaoServico registroServico) {
        this.registroServico = registroServico;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody RegistroProducaoDTO dto) {

        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(registroServico.cadastrar(dto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<RegistroProducaoRespostaDTO>> listar() {
        return ResponseEntity.ok(registroServico.listar());
    }

    @GetMapping("/relatorio")
    public ResponseEntity<?> relatorioPorGrupoEPeriodo(
            @RequestParam Long grupoId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataInicial,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataFinal) {

        try {
            return ResponseEntity.ok(
                    registroServico.relatorioPorGrupoEPeriodo(
                            grupoId,
                            dataInicial,
                            dataFinal
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (NoSuchElementException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
