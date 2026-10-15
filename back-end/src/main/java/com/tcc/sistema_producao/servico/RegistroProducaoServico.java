
package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.RegistroProducaoDTO;
import com.tcc.sistema_producao.dto.RegistroProducaoRespostaDTO;
import com.tcc.sistema_producao.entidade.GrupoProducao;
import com.tcc.sistema_producao.entidade.Produto;
import com.tcc.sistema_producao.entidade.RegistroProducao;
import com.tcc.sistema_producao.repositorio.GrupoProducaoRepositorio;
import com.tcc.sistema_producao.repositorio.ProdutoRepositorio;
import com.tcc.sistema_producao.repositorio.RegistroProducaoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RegistroProducaoServico {

    private final RegistroProducaoRepositorio registroRepositorio;
    private final GrupoProducaoRepositorio grupoRepositorio;
    private final ProdutoRepositorio produtoRepositorio;
    private final AuditoriaServico auditoriaServico;

    public RegistroProducaoServico(
            RegistroProducaoRepositorio registroRepositorio,
            GrupoProducaoRepositorio grupoRepositorio,
            ProdutoRepositorio produtoRepositorio,
            AuditoriaServico auditoriaServico) {

        this.registroRepositorio = registroRepositorio;
        this.grupoRepositorio = grupoRepositorio;
        this.produtoRepositorio = produtoRepositorio;
        this.auditoriaServico = auditoriaServico;
    }

    @Transactional
    public RegistroProducaoRespostaDTO cadastrar(
            RegistroProducaoDTO dto) {

        if (dto.quantidadeDefeituosa()
                > dto.quantidadeProduzida()) {

            throw new IllegalArgumentException(
                    "A quantidade defeituosa não pode ser maior que a quantidade produzida"
            );
        }

        GrupoProducao grupo = grupoRepositorio
                .findById(dto.grupoProducaoId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Grupo de produção não encontrado"
                        ));

        Produto produto = produtoRepositorio
                .findById(dto.produtoId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Produto não encontrado"
                        ));

        RegistroProducao registro = new RegistroProducao();

        registro.setGrupoProducao(grupo);
        registro.setProduto(produto);
        registro.setDataProducao(dto.dataProducao());
        registro.setQuantidadeProduzida(
                dto.quantidadeProduzida()
        );
        registro.setQuantidadeDefeituosa(
                dto.quantidadeDefeituosa()
        );

        RegistroProducao salvo =
                registroRepositorio.save(registro);

        auditoriaServico.registrarUsuarioAtual(
                "REGISTRO_PRODUCAO",
                "Produção de "
                        + salvo.getQuantidadeProduzida()
                        + " peças registrada para o grupo "
                        + grupo.getNome()
                        + " e produto "
                        + produto.getCodigo()
        );

        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<RegistroProducaoRespostaDTO> listar() {

        return registroRepositorio
                .findAllByOrderByDataProducaoDesc()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RegistroProducaoRespostaDTO> relatorioPorGrupoEPeriodo(
            Long grupoId,
            LocalDate dataInicial,
            LocalDate dataFinal) {

        if (dataInicial == null || dataFinal == null) {
            throw new IllegalArgumentException(
                    "As datas inicial e final são obrigatórias"
            );
        }

        if (dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final"
            );
        }

        grupoRepositorio.findById(grupoId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Grupo de produção não encontrado"
                        ));

        return registroRepositorio
                .findByGrupoProducao_IdAndDataProducaoBetweenOrderByDataProducaoDesc(
                        grupoId,
                        dataInicial,
                        dataFinal
                )
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private RegistroProducaoRespostaDTO converterParaDTO(
            RegistroProducao registro) {

        BigDecimal percentual = BigDecimal
                .valueOf(registro.getQuantidadeDefeituosa())
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(
                                registro.getQuantidadeProduzida()
                        ),
                        2,
                        RoundingMode.HALF_UP
                );

        return new RegistroProducaoRespostaDTO(
                registro.getId(),
                registro.getGrupoProducao().getId(),
                registro.getGrupoProducao().getNome(),
                registro.getProduto().getId(),
                registro.getProduto().getCodigo(),
                registro.getProduto().getDescricao(),
                registro.getDataProducao(),
                registro.getQuantidadeProduzida(),
                registro.getQuantidadeDefeituosa(),
                percentual
        );
    }
}
