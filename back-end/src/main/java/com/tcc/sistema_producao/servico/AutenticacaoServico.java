package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.LoginDTO;
import com.tcc.sistema_producao.dto.LoginRespostaDTO;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoServico {

    private final AuthenticationManager authenticationManager;
    private final FuncionarioRepositorio funcionarioRepositorio;
    private final JwtServico jwtServico;
    private final AuditoriaServico auditoriaServico;

    public AutenticacaoServico(
            AuthenticationManager authenticationManager,
            FuncionarioRepositorio funcionarioRepositorio,
            JwtServico jwtServico,
            AuditoriaServico auditoriaServico) {

        this.authenticationManager = authenticationManager;
        this.funcionarioRepositorio = funcionarioRepositorio;
        this.jwtServico = jwtServico;
        this.auditoriaServico = auditoriaServico;
    }

    public LoginRespostaDTO login(LoginDTO loginDTO) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.matricula(),
                        loginDTO.senha()
                )
        );

        Funcionario funcionario = funcionarioRepositorio
                .findByMatriculaAndAtivoTrue(loginDTO.matricula())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Funcionário não encontrado"
                        ));

        String token = jwtServico.gerarToken(
                funcionario.getMatricula(),
                funcionario.getPerfil()
        );

        auditoriaServico.registrar(
                funcionario.getMatricula(),
                "LOGIN",
                "Login realizado com sucesso"
        );

        return new LoginRespostaDTO(token);
    }
}