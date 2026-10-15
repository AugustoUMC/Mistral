package com.tcc.sistema_producao.configuracao;

import com.tcc.sistema_producao.servico.FuncionarioDetalhesServico;
import com.tcc.sistema_producao.servico.JwtServico;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAutenticacaoFiltro extends OncePerRequestFilter {

    private final JwtServico jwtServico;
    private final FuncionarioDetalhesServico funcionarioDetalhesServico;

    public JwtAutenticacaoFiltro(
            JwtServico jwtServico,
            FuncionarioDetalhesServico funcionarioDetalhesServico) {
        this.jwtServico = jwtServico;
        this.funcionarioDetalhesServico = funcionarioDetalhesServico;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String cabecalho = request.getHeader("Authorization");

        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = cabecalho.substring(7);

        try {
            String matricula = jwtServico.extrairMatricula(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails funcionario =
                        funcionarioDetalhesServico.loadUserByUsername(matricula);

                UsernamePasswordAuthenticationToken autenticacao =
                        new UsernamePasswordAuthenticationToken(
                                funcionario,
                                null,
                                funcionario.getAuthorities()
                        );

                autenticacao.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(autenticacao);
            }

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}