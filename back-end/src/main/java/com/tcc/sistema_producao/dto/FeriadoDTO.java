package com.tcc.sistema_producao.dto;

public record FeriadoDTO(
        String date,
        String name,
        String type,
        String weekday
) {
}