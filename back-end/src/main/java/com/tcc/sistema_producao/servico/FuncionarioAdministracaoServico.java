package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.EnderecoFuncionarioDTO;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class FuncionarioAdministracaoServico {

    private final FuncionarioRepositorio funcionarioRepositorio;
    private final AuditoriaServico auditoriaServico;
    private final AlocacaoFuncionarioGrupoServico alocacaoServico;

    public FuncionarioAdministracaoServico(
            FuncionarioRepositorio funcionarioRepositorio,
            AuditoriaServico auditoriaServico,
            AlocacaoFuncionarioGrupoServico alocacaoServico) {

        this.funcionarioRepositorio = funcionarioRepositorio;
        this.auditoriaServico = auditoriaServico;
        this.alocacaoServico = alocacaoServico;
    }

    @Transactional
    public void inativar(Long id) {

        Funcionario funcionario = obterFuncionario(id);

        if (Boolean.FALSE.equals(funcionario.getAtivo())) {
            return;
        }

        funcionario.setAtivo(false);
        funcionarioRepositorio.save(funcionario);

        int associacoesEncerradas =
                alocacaoServico.encerrarAssociacoesAtuais(funcionario.getId());

        auditoriaServico.registrarUsuarioAtual(
                "INATIVACAO_FUNCIONARIO",
                "Funcionário " + funcionario.getMatricula()
                        + " inativado. Associações atuais encerradas: "
                        + associacoesEncerradas + "."
        );
    }

    @Transactional(readOnly = true)
    public EnderecoFuncionarioDTO consultarEndereco(Long id) {

        Funcionario funcionario = obterFuncionario(id);

        return converterEndereco(funcionario);
    }

    @Transactional
    public EnderecoFuncionarioDTO atualizarEndereco(
            Long id,
            EnderecoFuncionarioDTO dados) {

        Funcionario funcionario = obterFuncionario(id);

        funcionario.setCep(normalizarCep(dados.cep()));
        funcionario.setLogradouro(normalizarTexto(dados.logradouro()));
        funcionario.setNumero(normalizarTexto(dados.numero()));
        funcionario.setComplemento(normalizarTexto(dados.complemento()));
        funcionario.setBairro(normalizarTexto(dados.bairro()));
        funcionario.setCidade(normalizarTexto(dados.cidade()));
        funcionario.setUf(normalizarUf(dados.uf()));

        Funcionario funcionarioSalvo =
                funcionarioRepositorio.save(funcionario);

        auditoriaServico.registrarUsuarioAtual(
                "ATUALIZACAO_ENDERECO_FUNCIONARIO",
                "Endereço do funcionário "
                        + funcionarioSalvo.getMatricula()
                        + " atualizado."
        );

        return converterEndereco(funcionarioSalvo);
    }

    private Funcionario obterFuncionario(Long id) {
        return funcionarioRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Funcionário não encontrado."
                ));
    }

    private EnderecoFuncionarioDTO converterEndereco(
            Funcionario funcionario) {

        return new EnderecoFuncionarioDTO(
                funcionario.getCep(),
                funcionario.getLogradouro(),
                funcionario.getNumero(),
                funcionario.getComplemento(),
                funcionario.getBairro(),
                funcionario.getCidade(),
                funcionario.getUf()
        );
    }

    private String normalizarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    private String normalizarCep(String cep) {
        String valor = normalizarTexto(cep);

        if (valor == null) {
            return null;
        }

        String digitos = valor.replaceAll("\\D", "");

        if (digitos.length() == 8) {
            return digitos.substring(0, 5) + "-" + digitos.substring(5);
        }

        return valor;
    }

    private String normalizarUf(String uf) {
        String valor = normalizarTexto(uf);
        return valor == null ? null : valor.toUpperCase(Locale.ROOT);
    }
}