package com.tcc.sistema_producao.controlador;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste")
public class TesteControlador {

    @GetMapping("/protegido")
    public String protegido() {
        return "Acesso autorizado ";
    }
}