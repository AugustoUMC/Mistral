package com.tcc.sistema_producao.configuracao;

import com.tcc.sistema_producao.servico.FuncionarioDetalhesServico;
import com.tcc.sistema_producao.servico.JwtServico;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAutenticacaoFiltro extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtAutenticacaoFiltro.class);

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
            FilterChain filterChain)
            throws ServletException, IOException {

        String cabecalho = request.getHeader("Authorization");

        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {

            if (requisicaoProtegidaEmDiagnostico(request)) {
                logger.warn(
                        "Requisição {} {} recebida sem token Bearer.",
                        request.getMethod(),
                        request.getRequestURI()
                );
            }

            filterChain.doFilter(request, response);
            return;
        }

        String token = cabecalho.substring(7).trim();

        try {

            if (token.isBlank()) {
                throw new IllegalArgumentException(
                        "O token Bearer está vazio."
                );
            }

            String matricula = jwtServico.extrairMatricula(token);

            Authentication autenticacaoAtual =
                    SecurityContextHolder.getContext().getAuthentication();

            if (autenticacaoAtual == null
                    || autenticacaoAtual instanceof AnonymousAuthenticationToken) {

                UserDetails funcionario =
                        funcionarioDetalhesServico.loadUserByUsername(matricula);

                UsernamePasswordAuthenticationToken autenticacao =
                        new UsernamePasswordAuthenticationToken(
                                funcionario,
                                null,
                                funcionario.getAuthorities()
                        );

                autenticacao.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(autenticacao);

                logger.info(
                        "JWT autenticado. Matrícula: {} | Autoridades: {} | Requisição: {} {}",
                        funcionario.getUsername(),
                        funcionario.getAuthorities(),
                        request.getMethod(),
                        request.getRequestURI()
                );

            } else {

                logger.info(
                        "Autenticação já existente. Principal: {} | Autoridades: {} | Requisição: {} {}",
                        autenticacaoAtual.getName(),
                        autenticacaoAtual.getAuthorities(),
                        request.getMethod(),
                        request.getRequestURI()
                );
            }

        } catch (Exception e) {

            SecurityContextHolder.clearContext();

            logger.error(
                    "Falha na autenticação JWT. Requisição: {} {} | Tipo: {} | Motivo: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token inválido ou usuário sem acesso ao sistema."
            );

            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requisicaoProtegidaEmDiagnostico(
            HttpServletRequest request) {

        String caminho = request.getRequestURI();

        return caminho.equals("/jornadas")
                || caminho.startsWith("/jornadas/")
                || caminho.matches(
                "/funcionarios/\\d+/alocacoes-grupos"
        );
    }
}