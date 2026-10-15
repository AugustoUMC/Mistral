package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.repositorio.GrupoProducaoRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrupoProducaoServico {

    private final GrupoProducaoRepositorio grupoProducaoRepositorio;
    private final AuditoriaServico auditoriaServico;

    public GrupoProducaoServico(
            GrupoProducaoRepositorio grupoProducaoRepositorio,
            AuditoriaServico auditoriaServico) {

        this.grupoProducaoRepositorio = grupoProducaoRepositorio;
        this.auditoriaServico = auditoriaServico;
    }

    public GrupoProducao cadastrar(GrupoProducao grupoProducao) {

        if (grupoProducaoRepositorio.existsByNome(grupoProducao.getNome())) {
            throw new IllegalArgumentException(
                    "O nome do grupo já está cadastrado"
            );
        }

        grupoProducao.setAtivo(true);

        GrupoProducao grupoSalvo =
                grupoProducaoRepositorio.save(grupoProducao);

        auditoriaServico.registrarUsuarioAtual(
                "CADASTRO_GRUPO",
                "Grupo " + grupoSalvo.getNome() + " cadastrado"
        );

        return grupoSalvo;
    }

    public List<GrupoProducao> listar() {
        return grupoProducaoRepositorio.findAll();
    }

    public GrupoProducao buscarPorId(Long id) {
        return grupoProducaoRepositorio.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Grupo de produção não encontrado"
                        ));
    }

    public GrupoProducao atualizar(Long id, GrupoProducao dados) {

        GrupoProducao grupo = buscarPorId(id);

        if (!grupo.getNome().equals(dados.getNome())
                && grupoProducaoRepositorio.existsByNome(dados.getNome())) {

            throw new IllegalArgumentException(
                    "O nome do grupo já está cadastrado"
            );
        }

        String nomeAnterior = grupo.getNome();

        grupo.setNome(dados.getNome());
        grupo.setAtivo(dados.getAtivo());

        GrupoProducao grupoAtualizado =
                grupoProducaoRepositorio.save(grupo);

        auditoriaServico.registrarUsuarioAtual(
                "ALTERACAO_GRUPO",
                "Grupo " + nomeAnterior
                        + " alterado para "
                        + grupoAtualizado.getNome()
        );

        return grupoAtualizado;
    }

    public void excluir(Long id) {

        GrupoProducao grupo = buscarPorId(id);

        grupoProducaoRepositorio.delete(grupo);

        auditoriaServico.registrarUsuarioAtual(
                "EXCLUSAO_GRUPO",
                "Grupo " + grupo.getNome() + " excluído"
        );
    }
}