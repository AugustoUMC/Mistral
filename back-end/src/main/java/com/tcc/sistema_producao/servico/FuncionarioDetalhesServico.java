package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FuncionarioDetalhesServico implements UserDetailsService {

    private final FuncionarioRepositorio funcionarioRepositorio;

    public FuncionarioDetalhesServico(FuncionarioRepositorio funcionarioRepositorio) {
        this.funcionarioRepositorio = funcionarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String matricula) throws UsernameNotFoundException {

        Funcionario funcionario = funcionarioRepositorio
                .findByMatriculaAndAtivoTrue(matricula)
                .orElseThrow(() -> new UsernameNotFoundException("Funcionário não encontrado"));

        if (!funcionario.getAcessoSistema()) {
            throw new UsernameNotFoundException("Funcionário sem acesso ao sistema");
        }

        return User.builder()
                .username(funcionario.getMatricula())
                .password(funcionario.getSenha())
                .roles(funcionario.getPerfil())
                .build();
    }
}