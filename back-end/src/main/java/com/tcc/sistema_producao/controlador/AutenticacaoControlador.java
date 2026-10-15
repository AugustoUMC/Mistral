package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.dto.CadastroFuncionarioDTO;
import com.tcc.sistema_producao.dto.LoginDTO;
import com.tcc.sistema_producao.dto.LoginRespostaDTO;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.servico.AutenticacaoServico;
import com.tcc.sistema_producao.servico.CadastroFuncionarioServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/autenticacao")
public class AutenticacaoControlador {

    private final AutenticacaoServico autenticacaoServico;
    private final CadastroFuncionarioServico cadastroFuncionarioServico;

    public AutenticacaoControlador(
            AutenticacaoServico autenticacaoServico,
            CadastroFuncionarioServico cadastroFuncionarioServico) {
        this.autenticacaoServico = autenticacaoServico;
        this.cadastroFuncionarioServico = cadastroFuncionarioServico;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(
            @Valid @RequestBody CadastroFuncionarioDTO cadastroDTO) {

        try {
            Funcionario funcionario =
                    cadastroFuncionarioServico.cadastrar(cadastroDTO);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(funcionario);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRespostaDTO> login(
            @Valid @RequestBody LoginDTO loginDTO) {

        return ResponseEntity.ok(
                autenticacaoServico.login(loginDTO)
        );
    }
}