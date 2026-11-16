package com.tcc.sistema_producao.servico;


import com.tcc.sistema_producao.dto.CadastroFuncionarioDTO;
import com.tcc.sistema_producao.dto.EnderecoFuncionarioDTO;
import com.tcc.sistema_producao.entidade.Funcionario;
import com.tcc.sistema_producao.repositorio.FuncionarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CadastroFuncionarioServico {

    private final FuncionarioRepositorio funcionarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaServico auditoriaServico;

    public CadastroFuncionarioServico(
            FuncionarioRepositorio funcionarioRepositorio,
            PasswordEncoder passwordEncoder,
            AuditoriaServico auditoriaServico) {

        this.funcionarioRepositorio = funcionarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaServico = auditoriaServico;
    }

    public Funcionario cadastrar(CadastroFuncionarioDTO dto) {

        if (funcionarioRepositorio
                .findByMatricula(dto.matricula())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "A matrícula já está cadastrada"
            );
        }

        boolean acessoSistema =
                Boolean.TRUE.equals(dto.acessoSistema());

        if (acessoSistema
                && (dto.senha() == null || dto.senha().isBlank())) {

            throw new IllegalArgumentException(
                    "A senha é obrigatória para funcionários com acesso ao sistema"
            );
        }

        Funcionario funcionario = new Funcionario();

        funcionario.setNome(dto.nome());
        funcionario.setMatricula(dto.matricula());
        funcionario.setCargo(dto.cargo());
        funcionario.setSalario(dto.salario());
        funcionario.setAtivo(true);
        funcionario.setAcessoSistema(acessoSistema);
        funcionario.setPerfil("USUARIO");

        aplicarEndereco(funcionario, dto.endereco());

        if (acessoSistema) {
            funcionario.setSenha(
                    passwordEncoder.encode(dto.senha())
            );
        } else {
            funcionario.setSenha(null);
        }

        Funcionario funcionarioSalvo =
                funcionarioRepositorio.save(funcionario);

        auditoriaServico.registrarUsuarioAtual(
                "CADASTRO_FUNCIONARIO",
                "Funcionário "
                        + funcionarioSalvo.getMatricula()
                        + " cadastrado. Acesso ao sistema: "
                        + (acessoSistema
                        ? "habilitado"
                        : "desabilitado")
        );

        return funcionarioSalvo;
    }

    private void aplicarEndereco(
            Funcionario funcionario,
            EnderecoFuncionarioDTO endereco) {

        if (endereco == null) {
            return;
        }

        funcionario.setCep(normalizarCep(endereco.cep()));
        funcionario.setLogradouro(normalizarTexto(endereco.logradouro()));
        funcionario.setNumero(normalizarTexto(endereco.numero()));
        funcionario.setComplemento(normalizarTexto(endereco.complemento()));
        funcionario.setBairro(normalizarTexto(endereco.bairro()));
        funcionario.setCidade(normalizarTexto(endereco.cidade()));
        funcionario.setUf(normalizarUf(endereco.uf()));
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