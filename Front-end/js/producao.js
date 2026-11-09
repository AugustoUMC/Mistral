const enderecoServidor = "http://localhost:8080";

const formularioProducao = document.getElementById("formularioProducao");
const formularioConsulta = document.getElementById("formularioConsulta");

const grupoProducao = document.getElementById("grupoProducao");
const produto = document.getElementById("produto");
const dataProducao = document.getElementById("dataProducao");
const quantidadeProduzida = document.getElementById("quantidadeProduzida");
const quantidadeDefeituosa = document.getElementById("quantidadeDefeituosa");

const grupoConsulta = document.getElementById("grupoConsulta");
const dataInicial = document.getElementById("dataInicial");
const dataFinal = document.getElementById("dataFinal");

const listaRegistros = document.getElementById("listaRegistros");
const mensagemCadastro = document.getElementById("mensagemCadastro");
const mensagemConsulta = document.getElementById("mensagemConsulta");

let registros = [];

function obterToken() {
    return localStorage.getItem("token");
}

function hojeLocal() {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = String(hoje.getMonth() + 1).padStart(2, "0");
    const dia = String(hoje.getDate()).padStart(2, "0");

    return `${ano}-${mes}-${dia}`;
}

function primeiroDiaMes() {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = String(hoje.getMonth() + 1).padStart(2, "0");

    return `${ano}-${mes}-01`;
}

function exibirMensagem(elemento, texto, tipo = "erro") {
    elemento.textContent = texto;
    elemento.className = texto ? `mensagem ${tipo}` : "mensagem";
}

function formatarNumero(valor) {
    return Number(valor || 0).toLocaleString("pt-BR");
}

function formatarPercentual(valor) {
    return `${Number(valor || 0).toLocaleString("pt-BR", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    })}%`;
}

function formatarData(valor) {
    if (!valor) return "";

    const partes = String(valor).split("-");

    if (partes.length !== 3) {
        return valor;
    }

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

async function requisitarApi(caminho, opcoes = {}) {
    const token = obterToken();

    if (!token) {
        window.location.replace("login.html");
        return null;
    }

    const resposta = await fetch(`${enderecoServidor}${caminho}`, {
        ...opcoes,
        headers: {
            ...(opcoes.headers || {}),
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
        return "Seu usuário não tem permissão para realizar esta operação.";
    }

    const texto = await resposta.text();

    if (texto) {
        try {
            const dados = JSON.parse(texto);

            if (typeof dados === "string") {
                return dados;
            }

            return dados.detail ||
                dados.message ||
                dados.erro ||
                dados.title ||
                "Não foi possível concluir a operação.";
        } catch {
            return texto;
        }
    }

    if (resposta.status === 404) {
        return "O grupo ou produto informado não foi encontrado.";
    }

    return "Não foi possível concluir a operação.";
}

async function carregarGrupos() {
    try {
        const resposta = await requisitarApi("/grupos");

        if (!resposta) return;

        if (!resposta.ok) {
            exibirMensagem(
                mensagemCadastro,
                await obterMensagemErro(resposta)
            );
            return;
        }

        const grupos = await resposta.json();
        const gruposAtivos = grupos.filter(grupo => grupo.ativo !== false);

        grupoProducao.replaceChildren(
            new Option("Selecione um grupo", "")
        );

        grupoConsulta.replaceChildren(
            new Option("Selecione um grupo", "")
        );

        gruposAtivos.forEach(grupo => {
            grupoProducao.add(new Option(grupo.nome, grupo.id));
            grupoConsulta.add(new Option(grupo.nome, grupo.id));
        });

        if (!gruposAtivos.length) {
            exibirMensagem(
                mensagemCadastro,
                "Não existem grupos ativos cadastrados."
            );
        }
    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemCadastro,
            "Não foi possível carregar os grupos."
        );
    }
}

async function carregarProdutos() {
    try {
        const resposta = await requisitarApi("/produtos");

        if (!resposta) return;

        if (!resposta.ok) {
            exibirMensagem(
                mensagemCadastro,
                await obterMensagemErro(resposta)
            );
            return;
        }

        const produtos = await resposta.json();

        produto.replaceChildren(
            new Option("Selecione um produto", "")
        );

        produtos.forEach(item => {
            produto.add(
                new Option(
                    `${item.codigo} - ${item.descricao}`,
                    item.id
                )
            );
        });

        if (!produtos.length) {
            exibirMensagem(
                mensagemCadastro,
                "Não existem produtos cadastrados."
            );
        }
    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemCadastro,
            "Não foi possível carregar os produtos."
        );
    }
}

function atualizarResumo() {
    const totalProduzido = registros.reduce(
        (soma, registro) => soma + Number(registro.quantidadeProduzida || 0),
        0
    );

    const totalDefeituoso = registros.reduce(
        (soma, registro) => soma + Number(registro.quantidadeDefeituosa || 0),
        0
    );

    const percentual = totalProduzido > 0
        ? totalDefeituoso * 100 / totalProduzido
        : 0;

    document.getElementById("totalProduzido").textContent =
        formatarNumero(totalProduzido);

    document.getElementById("totalDefeituoso").textContent =
        formatarNumero(totalDefeituoso);

    document.getElementById("percentualDefeituoso").textContent =
        formatarPercentual(percentual);
}

function criarCelula(valor) {
    const celula = document.createElement("td");

    celula.textContent =
        valor === null || valor === undefined ? "" : String(valor);

    return celula;
}

function renderizarRegistros() {
    listaRegistros.replaceChildren();

    if (!registros.length) {
        listaRegistros.innerHTML =
            '<tr><td colspan="7">Nenhum registro encontrado.</td></tr>';

        atualizarResumo();
        return;
    }

    registros.forEach(registro => {
        const linha = document.createElement("tr");

        linha.appendChild(criarCelula(formatarData(registro.dataProducao)));
        linha.appendChild(criarCelula(registro.grupoProducaoNome));
        linha.appendChild(criarCelula(registro.produtoCodigo));
        linha.appendChild(criarCelula(registro.produtoDescricao));
        linha.appendChild(criarCelula(
            formatarNumero(registro.quantidadeProduzida)
        ));
        linha.appendChild(criarCelula(
            formatarNumero(registro.quantidadeDefeituosa)
        ));
        linha.appendChild(criarCelula(
            formatarPercentual(registro.percentualDefeituoso)
        ));

        listaRegistros.appendChild(linha);
    });

    atualizarResumo();
}

async function carregarTodosRegistros() {
    exibirMensagem(mensagemConsulta, "");

    listaRegistros.innerHTML =
        '<tr><td colspan="7">Carregando registros...</td></tr>';

    try {
        const resposta = await requisitarApi("/registros-producao");

        if (!resposta) return;

        if (!resposta.ok) {
            exibirMensagem(
                mensagemConsulta,
                await obterMensagemErro(resposta)
            );

            listaRegistros.innerHTML =
                '<tr><td colspan="7">Não foi possível carregar os registros.</td></tr>';

            return;
        }

        registros = await resposta.json();

        document.getElementById("descricaoLista").textContent =
            "Exibindo todos os registros cadastrados.";

        renderizarRegistros();
    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemConsulta,
            "Não foi possível conectar ao servidor."
        );

        listaRegistros.innerHTML =
            '<tr><td colspan="7">Falha na comunicação com o backend.</td></tr>';
    }
}

formularioProducao.addEventListener("submit", async evento => {
    evento.preventDefault();

    exibirMensagem(mensagemCadastro, "");

    const produzido = Number(quantidadeProduzida.value);
    const defeituoso = Number(quantidadeDefeituosa.value);

    if (!grupoProducao.value || !produto.value) {
        exibirMensagem(
            mensagemCadastro,
            "Selecione o grupo e o produto."
        );
        return;
    }

    if (!Number.isInteger(produzido) || produzido < 1) {
        exibirMensagem(
            mensagemCadastro,
            "A quantidade produzida deve ser um número inteiro maior que zero."
        );
        return;
    }

    if (!Number.isInteger(defeituoso) || defeituoso < 0) {
        exibirMensagem(
            mensagemCadastro,
            "A quantidade defeituosa deve ser um número inteiro igual ou maior que zero."
        );
        return;
    }

    if (defeituoso > produzido) {
        exibirMensagem(
            mensagemCadastro,
            "A quantidade defeituosa não pode ser maior que a quantidade produzida."
        );
        return;
    }

    if (!dataProducao.value) {
        exibirMensagem(
            mensagemCadastro,
            "Informe a data da produção."
        );
        return;
    }

    const dados = {
        grupoProducaoId: Number(grupoProducao.value),
        produtoId: Number(produto.value),
        dataProducao: dataProducao.value,
        quantidadeProduzida: produzido,
        quantidadeDefeituosa: defeituoso
    };

    const botao = document.getElementById("botaoRegistrar");
    botao.disabled = true;

    try {
        const resposta = await requisitarApi("/registros-producao", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        if (!resposta) return;

        if (!resposta.ok) {
            exibirMensagem(
                mensagemCadastro,
                await obterMensagemErro(resposta)
            );
            return;
        }

        exibirMensagem(
            mensagemCadastro,
            "Registro de produção cadastrado com sucesso.",
            "sucesso"
        );

        const dataMantida = dataProducao.value;

        formularioProducao.reset();
        dataProducao.value = dataMantida;
        quantidadeDefeituosa.value = "0";

        await carregarTodosRegistros();
    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemCadastro,
            "Não foi possível registrar a produção. Verifique a conexão com o backend."
        );
    } finally {
        botao.disabled = false;
    }
});

formularioConsulta.addEventListener("submit", async evento => {
    evento.preventDefault();

    exibirMensagem(mensagemConsulta, "");

    if (!grupoConsulta.value) {
        exibirMensagem(mensagemConsulta, "Selecione um grupo.");
        return;
    }

    if (!dataInicial.value || !dataFinal.value) {
        exibirMensagem(
            mensagemConsulta,
            "Informe a data inicial e a data final."
        );
        return;
    }

    if (dataInicial.value > dataFinal.value) {
        exibirMensagem(
            mensagemConsulta,
            "A data inicial não pode ser posterior à data final."
        );
        return;
    }

    const parametros = new URLSearchParams({
        grupoId: grupoConsulta.value,
        dataInicial: dataInicial.value,
        dataFinal: dataFinal.value
    });

    listaRegistros.innerHTML =
        '<tr><td colspan="7">Consultando período...</td></tr>';

    try {
        const resposta = await requisitarApi(
            `/registros-producao/relatorio?${parametros.toString()}`
        );

        if (!resposta) return;

        if (!resposta.ok) {
            exibirMensagem(
                mensagemConsulta,
                await obterMensagemErro(resposta)
            );

            listaRegistros.innerHTML =
                '<tr><td colspan="7">Não foi possível consultar o período.</td></tr>';

            return;
        }

        registros = await resposta.json();

        const nomeGrupo = grupoConsulta.selectedOptions[0].textContent;

        document.getElementById("descricaoLista").textContent =
            `Registros do grupo ${nomeGrupo}, de ${formatarData(dataInicial.value)} a ${formatarData(dataFinal.value)}.`;

        renderizarRegistros();

        exibirMensagem(
            mensagemConsulta,
            `${registros.length} registro(s) encontrado(s).`,
            "info"
        );
    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemConsulta,
            "Não foi possível consultar o período."
        );
    }
});

document.getElementById("botaoMostrarTodos").addEventListener(
    "click",
    carregarTodosRegistros
);

document.getElementById("botaoAtualizar").addEventListener(
    "click",
    carregarTodosRegistros
);

document.getElementById("botaoSair").addEventListener("click", () => {
    localStorage.removeItem("token");
    window.location.replace("login.html");
});

dataProducao.value = hojeLocal();
dataInicial.value = primeiroDiaMes();
dataFinal.value = hojeLocal();

Promise.all([
    carregarGrupos(),
    carregarProdutos(),
    carregarTodosRegistros()
]);