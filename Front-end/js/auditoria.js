
(function () {
    "use strict";

    const enderecoServidor = "http://localhost:8080";
    const formulario = document.getElementById("formularioFiltrosAuditoria");
    const matricula = document.getElementById("filtroMatricula");
    const operacao = document.getElementById("filtroOperacao");
    const dataInicial = document.getElementById("filtroDataInicial");
    const dataFinal = document.getElementById("filtroDataFinal");
    const mensagem = document.getElementById("mensagemAuditoria");
    const lista = document.getElementById("listaAuditoria");
    const resumo = document.getElementById("resumoAuditoria");
    const paginaTexto = document.getElementById("paginaAuditoria");
    const anterior = document.getElementById("botaoAnteriorAuditoria");
    const proximo = document.getElementById("botaoProximoAuditoria");
    const atualizar = document.getElementById("botaoAtualizarAuditoria");
    const limpar = document.getElementById("botaoLimparAuditoria");
    const sair = document.getElementById("botaoSair");
    const tamanhoPagina = 20;

    let paginaAtual = 0;
    let totalPaginas = 0;
    let carregando = false;

    function obterPerfil(token) {
        try {
            const partes = token.split(".");
            if (partes.length !== 3) return null;
            let payload = partes[1].replace(/-/g, "+").replace(/_/g, "/");
            payload += "=".repeat((4 - payload.length % 4) % 4);
            const bytes = Uint8Array.from(atob(payload), caractere => caractere.charCodeAt(0));
            const dados = JSON.parse(new TextDecoder().decode(bytes));
            if (dados.exp && Number(dados.exp) * 1000 <= Date.now()) return null;
            return String(dados.perfil || "").toUpperCase();
        } catch (erro) {
            return null;
        }
    }

    function exibirMensagem(texto, sucesso = false) {
        mensagem.textContent = texto;
        mensagem.hidden = !texto;
        mensagem.classList.toggle("erro", Boolean(texto) && !sucesso);
        mensagem.classList.toggle("sucesso", Boolean(texto) && sucesso);
    }

    function controlarBotoes() {
        anterior.disabled = carregando || paginaAtual <= 0;
        proximo.disabled = carregando || paginaAtual >= totalPaginas - 1;
        atualizar.disabled = carregando;
        formulario.querySelector("button[type=submit]").disabled = carregando;
    }

    function exibirEstado(texto) {
        const linha = document.createElement("tr");
        const celula = document.createElement("td");
        celula.colSpan = 4;
        celula.textContent = texto;
        linha.appendChild(celula);
        lista.replaceChildren(linha);
    }

    function criarCelula(texto) {
        const celula = document.createElement("td");
        celula.textContent = texto === null || texto === undefined || texto === "" ? "—" : String(texto);
        return celula;
    }

    function formatarDataHora(valor) {
        if (!valor) return "—";
        const data = new Date(valor);
        if (Number.isNaN(data.getTime())) return String(valor);
        return data.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "medium" });
    }

    function renderizarRegistros(registros) {
        lista.replaceChildren();
        if (!registros.length) {
            exibirEstado("Nenhum registro encontrado para os filtros informados.");
            return;
        }

        for (const registro of registros) {
            const linha = document.createElement("tr");
            const celulaOperacao = document.createElement("td");
            const etiqueta = document.createElement("span");
            etiqueta.className = "etiqueta-operacao";
            etiqueta.textContent = registro.operacao || "—";
            celulaOperacao.appendChild(etiqueta);
            linha.append(
                criarCelula(formatarDataHora(registro.dataHora)),
                criarCelula(registro.matricula),
                celulaOperacao,
                criarCelula(registro.descricao)
            );
            lista.appendChild(linha);
        }
    }

    function criarParametros() {
        const parametros = new URLSearchParams();
        parametros.set("pagina", String(paginaAtual));
        parametros.set("tamanho", String(tamanhoPagina));
        if (matricula.value.trim()) parametros.set("matricula", matricula.value.trim());
        if (operacao.value.trim()) parametros.set("operacao", operacao.value.trim());
        if (dataInicial.value) parametros.set("dataInicial", dataInicial.value);
        if (dataFinal.value) parametros.set("dataFinal", dataFinal.value);
        return parametros;
    }

    async function consultar() {
        if (carregando) return;
        if (dataInicial.value && dataFinal.value && dataInicial.value > dataFinal.value) {
            exibirMensagem("A data inicial não pode ser posterior à data final.");
            return;
        }

        carregando = true;
        controlarBotoes();
        exibirMensagem("");
        exibirEstado("Carregando registros de auditoria...");
        resumo.textContent = "Consultando histórico...";

        try {
            const token = localStorage.getItem("token");
            if (!token) {
                window.location.replace("login.html");
                return;
            }
            const resposta = await fetch(`${enderecoServidor}/auditoria?${criarParametros()}`, {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (resposta.status === 401) {
                localStorage.removeItem("token");
                window.location.replace("login.html");
                return;
            }
            if (resposta.status === 403) {
                exibirMensagem("Acesso negado. A consulta de auditoria é exclusiva para administradores.");
                exibirEstado("Sem permissão para consultar auditoria.");
                return;
            }
            if (!resposta.ok) throw new Error(`Falha na consulta (HTTP ${resposta.status}).`);

            const dados = await resposta.json();
            const registros = Array.isArray(dados.content) ? dados.content : [];
            totalPaginas = Number(dados.totalPages) || 0;
            paginaAtual = Number(dados.number) || 0;
            const totalRegistros = Number(dados.totalElements) || 0;

            renderizarRegistros(registros);
            resumo.textContent = `${totalRegistros.toLocaleString("pt-BR")} registro(s) encontrado(s)`;
            paginaTexto.textContent = totalPaginas ? `Página ${paginaAtual + 1} de ${totalPaginas}` : "Nenhuma página";
        } catch (erro) {
            console.error(erro);
            exibirMensagem("Não foi possível consultar a auditoria. Verifique se o backend está em execução.");
            exibirEstado("Não foi possível carregar os registros.");
            resumo.textContent = "Consulta indisponível";
        } finally {
            carregando = false;
            controlarBotoes();
        }
    }

    formulario.addEventListener("submit", evento => {
        evento.preventDefault();
        paginaAtual = 0;
        consultar();
    });

    limpar.addEventListener("click", () => {
        formulario.reset();
        paginaAtual = 0;
        consultar();
    });

    atualizar.addEventListener("click", consultar);

    anterior.addEventListener("click", () => {
        if (paginaAtual > 0) {
            paginaAtual--;
            consultar();
        }
    });

    proximo.addEventListener("click", () => {
        if (paginaAtual + 1 < totalPaginas) {
            paginaAtual++;
            consultar();
        }
    });

    sair.addEventListener("click", () => {
        localStorage.removeItem("token");
        window.location.replace("login.html");
    });

    const token = localStorage.getItem("token");
    if (!token) {
        window.location.replace("login.html");
    } else if (obterPerfil(token) !== "ADMIN") {
        window.location.replace("index.html");
    } else {
        consultar();
    }
})();
