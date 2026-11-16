
package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.entidade.Auditoria;
import com.tcc.sistema_producao.repositorio.AuditoriaRepositorio;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaControlador {

    private static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");
    private final AuditoriaRepositorio auditoriaRepositorio;

    public AuditoriaControlador(AuditoriaRepositorio auditoriaRepositorio) {
        this.auditoriaRepositorio = auditoriaRepositorio;
    }

    @GetMapping
    public Page<Auditoria> consultar(
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String operacao,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {

        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parâmetros de paginação inválidos.");
        }

        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data inicial não pode ser posterior à final.");
        }

        String matriculaFiltro = normalizar(matricula);
        String operacaoFiltro = normalizar(operacao);

        OffsetDateTime inicio = dataInicial == null
                ? null
                : dataInicial.atStartOfDay(FUSO_HORARIO).toOffsetDateTime();

        OffsetDateTime fimExclusivo = dataFinal == null
                ? null
                : dataFinal.plusDays(1).atStartOfDay(FUSO_HORARIO).toOffsetDateTime();

        Specification<Auditoria> filtro = (raiz, consulta, criteria) -> {
            List<Predicate> condicoes = new ArrayList<>();

            if (matriculaFiltro != null) {
                condicoes.add(criteria.like(
                        criteria.lower(raiz.get("matricula")),
                        "%" + matriculaFiltro.toLowerCase() + "%"
                ));
            }

            if (operacaoFiltro != null) {
                condicoes.add(criteria.like(
                        criteria.lower(raiz.get("operacao")),
                        "%" + operacaoFiltro.toLowerCase() + "%"
                ));
            }

            if (inicio != null) {
                condicoes.add(criteria.greaterThanOrEqualTo(raiz.get("dataHora"), inicio));
            }

            if (fimExclusivo != null) {
                condicoes.add(criteria.lessThan(raiz.get("dataHora"), fimExclusivo));
            }

            return criteria.and(condicoes.toArray(new Predicate[0]));
        };

        PageRequest paginacao = PageRequest.of(
                pagina,
                tamanho,
                Sort.by(Sort.Order.desc("dataHora"), Sort.Order.desc("id"))
        );

        return auditoriaRepositorio.findAll(filtro, paginacao);
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
