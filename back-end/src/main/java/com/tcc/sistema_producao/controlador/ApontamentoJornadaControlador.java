package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.dto.ApontamentoJornadaDTO;
import com.tcc.sistema_producao.dto.ApontamentoJornadaRespostaDTO;
import com.tcc.sistema_producao.dto.JornadaFuncionarioOpcaoDTO;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import com.tcc.sistema_producao.servico.ApontamentoJornadaServico;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/jornadas")
public class ApontamentoJornadaControlador {

    private final ApontamentoJornadaServico jornadaServico;
    private final FuncionarioRepositorio funcionarioRepositorio;

    public ApontamentoJornadaControlador(
            ApontamentoJornadaServico jornadaServico,
            FuncionarioRepositorio funcionarioRepositorio) {

        this.jornadaServico = jornadaServico;
        this.funcionarioRepositorio = funcionarioRepositorio;
    }

    @GetMapping("/opcoes/funcionarios")
    public ResponseEntity<List<JornadaFuncionarioOpcaoDTO>> listarFuncionarios() {

        List<JornadaFuncionarioOpcaoDTO> funcionarios =
                funcionarioRepositorio.findAllByOrderByNomeAsc()
                        .stream()
                        .map(this::converterFuncionario)
                        .toList();

        return ResponseEntity.ok(funcionarios);
    }

    @GetMapping
    public ResponseEntity<?> pesquisar(
            @RequestParam(required = false) Long funcionarioId,
            @RequestParam(required = false) Long grupoId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataInicial,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataFinal) {

        try {
            List<ApontamentoJornadaRespostaDTO> resultado =
                    jornadaServico.pesquisar(
                            funcionarioId,
                            grupoId,
                            dataInicial,
                            dataFinal
                    );

            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody ApontamentoJornadaDTO dto) {

        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(jornadaServico.cadastrar(dto));

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ApontamentoJornadaDTO dto) {

        try {
            return ResponseEntity.ok(
                    jornadaServico.atualizar(id, dto)
            );

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    private JornadaFuncionarioOpcaoDTO converterFuncionario(
            Funcionario funcionario) {

        return new JornadaFuncionarioOpcaoDTO(
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getMatricula()
        );
    }
}