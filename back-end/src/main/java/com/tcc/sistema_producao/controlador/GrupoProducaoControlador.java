package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.servico.GrupoProducaoServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grupos")
public class GrupoProducaoControlador {

    private final GrupoProducaoServico grupoProducaoServico;

    public GrupoProducaoControlador(GrupoProducaoServico grupoProducaoServico) {
        this.grupoProducaoServico = grupoProducaoServico;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody GrupoProducao grupoProducao) {

        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(grupoProducaoServico.cadastrar(grupoProducao));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<GrupoProducao>> listar() {
        return ResponseEntity.ok(grupoProducaoServico.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {

        try {
            return ResponseEntity.ok(grupoProducaoServico.buscarPorId(id));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody GrupoProducao dados) {

        try {
            return ResponseEntity.ok(
                    grupoProducaoServico.atualizar(id, dados)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {

        try {
            grupoProducaoServico.excluir(id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}