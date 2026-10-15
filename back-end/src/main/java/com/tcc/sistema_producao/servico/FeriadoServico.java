package com.tcc.sistema_producao.servico;

import com.tcc.sistema_producao.dto.FeriadoDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Service
public class FeriadoServico {

    private final RestClient restClient;

    public FeriadoServico() {
        this.restClient = RestClient.create(
                "https://brasilapi.com.br"
        );
    }

    public List<FeriadoDTO> listarPorAno(int ano) {

        FeriadoDTO[] feriados = restClient
                .get()
                .uri("/api/feriados/v1/{ano}", ano)
                .retrieve()
                .body(FeriadoDTO[].class);

        if (feriados == null) {
            return List.of();
        }

        return Arrays.asList(feriados);
    }
}