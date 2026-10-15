package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.entidade.Produto;
import com.tcc.sistema_producao.servico.ProdutoServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoControlador {

    private final ProdutoServico produtoServico;

    public ProdutoControlador(ProdutoServico produtoServico) {
        this.produtoServico = produtoServico;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody Produto produto) {

        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(produtoServico.cadastrar(produto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listar() {
        return ResponseEntity.ok(produtoServico.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {

        try {
            return ResponseEntity.ok(produtoServico.buscarPorId(id));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Produto dados) {

        try {
            return ResponseEntity.ok(
                    produtoServico.atualizar(id, dados)
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
            produtoServico.excluir(id);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}