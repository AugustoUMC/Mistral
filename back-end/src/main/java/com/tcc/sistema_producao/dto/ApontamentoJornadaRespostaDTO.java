package com.tcc.sistema_producao.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record ApontamentoJornadaRespostaDTO(
        Long id,
        Long funcionarioId,
        String funcionarioNome,
        String matricula,
        Long grupoProducaoId,
        String grupoProducaoNome,
        LocalDate dataJornada,
        LocalDate dataFim,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime horarioInicio,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime horarioFim,

        Integer minutosBrutos,
        Integer intervaloMinutos,
        Integer minutosLiquidos
) {
}