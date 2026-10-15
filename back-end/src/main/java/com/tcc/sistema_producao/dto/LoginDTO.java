package com.tcc.sistema_producao.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(

        @NotBlank(message = "A matrícula é obrigatória")
        String matricula,

        @NotBlank(message = "A senha é obrigatória")
        String senha

) {
}