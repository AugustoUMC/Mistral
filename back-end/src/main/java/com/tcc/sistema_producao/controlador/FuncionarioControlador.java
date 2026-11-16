package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.dto.AtualizarGruposFuncionarioDTO;
import com.tcc.sistema_producao.dto.EnderecoFuncionarioDTO;
import com.tcc.sistema_producao.servico.AlocacaoFuncionarioGrupoServico;
import com.tcc.sistema_producao.servico.FuncionarioAdministracaoServico;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/funcionarios")
public class FuncionarioControlador {

    private final FuncionarioAdministracaoServico funcionarioServico;
    private final AlocacaoFuncionarioGrupoServico alocacaoServico;

    public FuncionarioControlador(
            FuncionarioAdministracaoServico funcionarioServico,
            AlocacaoFuncionarioGrupoServico alocacaoServico) {

        this.funcionarioServico = funcionarioServico;
        this.alocacaoServico = alocacaoServico;
    }

    @PutMapping("/{id}/inativar")
    public ResponseEntity<?> inativar(@PathVariable Long id) {

        try {
            funcionarioServico.inativar(id);

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/{id}/endereco")
    public ResponseEntity<?> consultarEndereco(@PathVariable Long id) {

        try {
            return ResponseEntity.ok(
                    funcionarioServico.consultarEndereco(id)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}/endereco")
    public ResponseEntity<?> atualizarEndereco(
            @PathVariable Long id,
            @Valid @RequestBody EnderecoFuncionarioDTO dados) {

        try {
            return ResponseEntity.ok(
                    funcionarioServico.atualizarEndereco(id, dados)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/{id}/alocacoes-grupos")
    public ResponseEntity<?> listarAlocacoes(@PathVariable Long id) {

        try {
            return ResponseEntity.ok(
                    alocacaoServico.listarHistorico(id)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}/grupos")
    public ResponseEntity<?> atualizarGrupos(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarGruposFuncionarioDTO dados) {

        try {
            return ResponseEntity.ok(
                    alocacaoServico.atualizarGrupos(
                            id,
                            dados.grupoIds()
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }
}