package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.entidade.Auditoria;
import com.tcc.sistema_producao.repositorio.AuditoriaRepositorio;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
public class AuditoriaServico {

    private final AuditoriaRepositorio auditoriaRepositorio;

    public AuditoriaServico(AuditoriaRepositorio auditoriaRepositorio) {
        this.auditoriaRepositorio = auditoriaRepositorio;
    }

    public void registrar(String matricula, String operacao, String descricao) {

        Auditoria auditoria = new Auditoria();

        auditoria.setMatricula(matricula);
        auditoria.setOperacao(operacao);
        auditoria.setDescricao(descricao);
        auditoria.setDataHora(
                OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"))
        );

        auditoriaRepositorio.save(auditoria);
    }

    public void registrarUsuarioAtual(String operacao, String descricao) {

        Authentication autenticacao =
                SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            return;
        }

        registrar(
                autenticacao.getName(),
                operacao,
                descricao
        );
    }
}