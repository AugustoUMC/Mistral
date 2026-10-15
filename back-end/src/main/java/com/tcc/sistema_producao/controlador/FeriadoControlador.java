package com.tcc.sistema_producao.controlador;

import com.tcc.sistema_producao.dto.FeriadoDTO;
import com.tcc.sistema_producao.servico.FeriadoServico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feriados")
public class FeriadoControlador {

    private final FeriadoServico feriadoServico;

    public FeriadoControlador(FeriadoServico feriadoServico) {
        this.feriadoServico = feriadoServico;
    }

    @GetMapping("/{ano}")
    public ResponseEntity<List<FeriadoDTO>> listar(
            @PathVariable int ano) {

        return ResponseEntity.ok(
                feriadoServico.listarPorAno(ano)
        );
    }
}