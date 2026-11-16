const enderecoServidor = "http://localhost:8080";

const formularioPeriodo = document.getElementById("formularioPeriodo");
const formularioMensal = document.getElementById("formularioMensal");
const mensagemRelatorio = document.getElementById("mensagemRelatorio");
const areaResultado = document.getElementById("areaResultado");

const dataInicial = document.getElementById("dataInicial");
const dataFinal = document.getElementById("dataFinal");
const mes = document.getElementById("mes");
const ano = document.getElementById("ano");

const botaoPeriodo = document.getElementById("botaoPeriodo");
const botaoMensal = document.getElementById("botaoMensal");

function obterToken() {
    return localStorage.getItem("token");
}

function dataLocalAtual() {
    const data = new Date();
    const anoAtual = data.getFullYear();
    const mesAtual = String(data.getMonth() + 1).padStart(2, "0");
    const diaAtual = String(data.getDate()).padStart(2, "0");

    return `${anoAtual}-${mesAtual}-${diaAtual}`;
}

function primeiroDiaDoMes() {
    const data = new Date();

    return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, "0")}-01`;
}

function formatarNumero(valor) {
    return Number(valor || 0).toLocaleString("pt-BR");
}

function formatarPercentual(valor) {
    if (valor === null || valor === undefined) {
        return "—";
    }

    return `${Number(valor).toLocaleString("pt-BR", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    })}%`;
}

function formatarData(valor) {
    if (!valor) {
        return "—";
    }

    const partes = String(valor).split("-");

    if (partes.length !== 3) {
        return valor;
    }

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

function exibirMensagem(texto, tipo = "erro") {
    mensagemRelatorio.textContent = texto;
    mensagemRelatorio.className = texto ? `mensagem ${tipo}` : "mensagem";
}

async function requisitarApi(caminho) {
    const token = obterToken();

    if (!token) {
        window.location.replace("login.html");
        return null;
    }

    const resposta = await fetch(`${enderecoServidor}${caminho}`, {
        method: "GET",
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    if (resposta.status === 401) {
        localStorage.removeItem("token");
        window.location.replace("login.html");
        return null;
    }

    return resposta;
}

async function obterMensagemErro(resposta) {
    if (resposta.status === 403) {
        return "Seu usuário não tem permissão para consultar este relatório.";
    }

    const texto = await resposta.text();

    if (texto) {
        try {
            const dados = JSON.parse(texto);

            if (typeof dados === "string") {
                return dados;
            }

            return dados.message ||
                dados.erro ||
                dados.detail ||
                dados.title ||
                "Não foi possível gerar o relatório.";
        } catch {
            return texto;
        }
    }

    return "Não foi possível gerar o relatório. Tente novamente.";
}

function criarCelula(valor) {
    const celula = document.createElement("td");
    celula.textContent =
        valor === null || valor === undefined ? "—" : String(valor);

    return celula;
}

function renderizarDetalhes(detalhes) {
    const lista = document.getElementById("listaDetalhes");
    lista.replaceChildren();

    if (!detalhes || detalhes.length === 0) {
        lista.innerHTML =
            '<tr><td colspan="8">Nenhum detalhe encontrado para este período.</td></tr>';
        return;
    }

    detalhes.forEach(item => {
        const linha = document.createElement("tr");

        linha.appendChild(criarCelula(item.grupoProducaoNome));
        linha.appendChild(criarCelula(item.produtoCodigo));
        linha.appendChild(criarCelula(item.produtoDescricao));
        linha.appendChild(criarCelula(formatarNumero(item.quantidadeMeta)));
        linha.appendChild(criarCelula(formatarNumero(item.quantidadeProduzida)));
        linha.appendChild(criarCelula(formatarNumero(item.quantidadeDefeituosa)));
        linha.appendChild(criarCelula(formatarPercentual(item.percentualMetaAtingida)));
        linha.appendChild(criarCelula(formatarPercentual(item.percentualDefeituoso)));

        lista.appendChild(linha);
    });
}

function renderizarFeriados(feriados, consultaConcluida) {
    const lista = document.getElementById("listaFeriados");
    const estado = document.getElementById("estadoFeriados");

    lista.replaceChildren();

    if (consultaConcluida) {
        estado.textContent = "Consulta de feriados concluída.";
    } else {
        estado.textContent =
            "A consulta de feriados foi parcialmente concluída. Alguns dados podem não estar disponíveis.";
    }

    if (!feriados || feriados.length === 0) {
        const linha = document.createElement("tr");
        const celula = document.createElement("td");

        celula.colSpan = 4;
        celula.textContent = consultaConcluida
            ? "Nenhum feriado encontrado no período."
            : "Não foi possível obter a lista de feriados deste período.";

        linha.appendChild(celula);
        lista.appendChild(linha);
        return;
    }

    feriados.forEach(feriado => {
        const linha = document.createElement("tr");

        linha.appendChild(criarCelula(formatarData(feriado.date)));
        linha.appendChild(criarCelula(feriado.weekday));
        linha.appendChild(criarCelula(feriado.name));
        linha.appendChild(criarCelula(feriado.type));

        lista.appendChild(linha);
    });
}

function renderizarRelatorio(relatorio) {
    document.getElementById("periodoConsultado").textContent =
        `Período de ${formatarData(relatorio.dataInicial)} até ${formatarData(relatorio.dataFinal)}.`;

    document.getElementById("totalMeta").textContent =
        formatarNumero(relatorio.quantidadeMetaTotal);

    document.getElementById("totalProduzido").textContent =
        formatarNumero(relatorio.quantidadeProduzidaTotal);

    document.getElementById("totalDefeituoso").textContent =
        formatarNumero(relatorio.quantidadeDefeituosaTotal);

    document.getElementById("percentualMeta").textContent =
        formatarPercentual(relatorio.percentualMetaAtingida);

    document.getElementById("percentualDefeituoso").textContent =
        formatarPercentual(relatorio.percentualDefeituoso);

    renderizarDetalhes(relatorio.detalhes);
    renderizarFeriados(
        relatorio.feriados,
        relatorio.consultaFeriadosConcluida
    );

    areaResultado.hidden = false;
}

async function gerarRelatorio(caminho, botao) {
    exibirMensagem("");
    botao.disabled = true;

    const textoOriginal = botao.textContent;
    botao.textContent = "Consultando...";

    try {
        const resposta = await requisitarApi(caminho);

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            exibirMensagem(await obterMensagemErro(resposta));
            return;
        }

        const relatorio = await resposta.json();

        renderizarRelatorio(relatorio);
        exibirMensagem(
            "Relatório gerado com sucesso.",
            "sucesso"
        );

        areaResultado.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            "Não foi possível conectar ao backend. Verifique se o servidor está executando."
        );
    } finally {
        botao.disabled = false;
        botao.textContent = textoOriginal;
    }
}

formularioPeriodo.addEventListener("submit", evento => {
    evento.preventDefault();

    if (!dataInicial.value || !dataFinal.value) {
        exibirMensagem("Informe as duas datas.");
        return;
    }

    if (dataInicial.value > dataFinal.value) {
        exibirMensagem(
            "A data inicial não pode ser posterior à data final."
        );
        return;
    }

    const parametros = new URLSearchParams({
        dataInicial: dataInicial.value,
        dataFinal: dataFinal.value
    });

    gerarRelatorio(
        `/relatorios/producao/global?${parametros.toString()}`,
        botaoPeriodo
    );
});

formularioMensal.addEventListener("submit", evento => {
    evento.preventDefault();

    const anoSelecionado = Number(ano.value);
    const mesSelecionado = Number(mes.value);

    if (
        !Number.isInteger(anoSelecionado) ||
        anoSelecionado < 2000 ||
        anoSelecionado > 2100
    ) {
        exibirMensagem("Informe um ano válido entre 2000 e 2100.");
        return;
    }

    if (
        !Number.isInteger(mesSelecionado) ||
        mesSelecionado < 1 ||
        mesSelecionado > 12
    ) {
        exibirMensagem("Selecione um mês válido.");
        return;
    }

    const parametros = new URLSearchParams({
        ano: String(anoSelecionado),
        mes: String(mesSelecionado)
    });

    gerarRelatorio(
        `/relatorios/producao/mensal?${parametros.toString()}`,
        botaoMensal
    );
});

document.getElementById("botaoImprimir").addEventListener("click", () => {
    window.print();
});

document.getElementById("botaoSair").addEventListener("click", () => {
    localStorage.removeItem("token");
    window.location.replace("login.html");
});

dataInicial.value = primeiroDiaDoMes();
dataFinal.value = dataLocalAtual();
ano.value = new Date().getFullYear();
mes.value = String(new Date().getMonth() + 1);