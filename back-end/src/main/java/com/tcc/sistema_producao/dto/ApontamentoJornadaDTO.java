package com.tcc.sistema_producao.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record ApontamentoJornadaDTO(

        @NotNull(message = "O funcionário é obrigatório.")
        @Positive(message = "O identificador do funcionário deve ser positivo.")
        Long funcionarioId,

        @NotNull(message = "O grupo de produção é obrigatório.")
        @Positive(message = "O identificador do grupo deve ser positivo.")
        Long grupoProducaoId,

        @NotNull(message = "A data da jornada é obrigatória.")
        LocalDate dataJornada,

        @NotNull(message = "O horário de início é obrigatório.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime horarioInicio,

        @NotNull(message = "O horário de término é obrigatório.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime horarioFim,

        @NotNull(message = "Informe a soma dos intervalos em minutos.")
        @Min(value = 0, message = "O intervalo não pode ser negativo.")
        Integer intervaloMinutos

) {
}