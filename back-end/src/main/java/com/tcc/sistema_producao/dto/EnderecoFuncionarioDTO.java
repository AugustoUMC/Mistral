package com.tcc.sistema_producao.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoFuncionarioDTO(

        @Size(max = 9, message = "O CEP deve ter no máximo 9 caracteres.")
        @Pattern(
                regexp = "^(?:\\d{5}-?\\d{3})?$",
                message = "Informe um CEP válido com 8 dígitos."
        )
        String cep,

        @Size(max = 150, message = "O logradouro deve ter no máximo 150 caracteres.")
        String logradouro,

        @Size(max = 20, message = "O número deve ter no máximo 20 caracteres.")
        String numero,

        @Size(max = 100, message = "O complemento deve ter no máximo 100 caracteres.")
        String complemento,

        @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres.")
        String bairro,

        @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres.")
        String cidade,

        @Pattern(
                regexp = "^(?:[A-Za-z]{2})?$",
                message = "A UF deve conter duas letras."
        )
        String uf
) {
}