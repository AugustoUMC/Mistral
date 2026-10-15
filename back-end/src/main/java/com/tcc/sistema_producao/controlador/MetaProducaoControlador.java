package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.dto.MetaProducaoDTO;
import com.tcc.sistema_producao.entidade.MetaProducao;
import com.tcc.sistema_producao.servico.MetaProducaoServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metas")
public class MetaProducaoControlador {

    private final MetaProducaoServico metaProducaoServico;

    public MetaProducaoControlador(MetaProducaoServico metaProducaoServico) {
        this.metaProducaoServico = metaProducaoServico;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody MetaProducaoDTO dto) {

        try {
            MetaProducao meta = metaProducaoServico.cadastrar(
                    dto.grupoProducaoId(),
                    dto.produtoId(),
                    dto.quantidadeMeta()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(meta);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<MetaProducao>> listar() {
        return ResponseEntity.ok(metaProducaoServico.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {

        try {
            return ResponseEntity.ok(
                    metaProducaoServico.buscarPorId(id)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/grupo/{grupoProducaoId}")
    public ResponseEntity<List<MetaProducao>> listarPorGrupo(
            @PathVariable Long grupoProducaoId) {

        return ResponseEntity.ok(
                metaProducaoServico.listarPorGrupo(grupoProducaoId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MetaProducaoDTO dto) {

        try {
            MetaProducao meta = metaProducaoServico.atualizar(
                    id,
                    dto.grupoProducaoId(),
                    dto.produtoId(),
                    dto.quantidadeMeta()
            );

            return ResponseEntity.ok(meta);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {

        try {
            metaProducaoServico.excluir(id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}