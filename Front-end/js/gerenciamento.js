const enderecoServidor = "http://localhost:8080";

const formularioGrupo = document.getElementById("formularioGrupo");
const formularioProduto = document.getElementById("formularioProduto");
const formularioMeta = document.getElementById("formularioMeta");

const nomeGrupo = document.getElementById("nomeGrupo");
const codigoProduto = document.getElementById("codigoProduto");
const descricaoProduto = document.getElementById("descricaoProduto");

const grupoMeta = document.getElementById("grupoMeta");
const produtoMeta = document.getElementById("produtoMeta");
const quantidadeMeta = document.getElementById("quantidadeMeta");

const mensagemGrupo = document.getElementById("mensagemGrupo");
const mensagemProduto = document.getElementById("mensagemProduto");
const mensagemMeta = document.getElementById("mensagemMeta");
const mensagemLista = document.getElementById("mensagemLista");

const listaMetas = document.getElementById("listaMetas");
const botaoAtualizar = document.getElementById("botaoAtualizar");
const botaoSair = document.getElementById("botaoSair");

const token = localStorage.getItem("token");

function verificarToken() {
    if (!token || token.trim() === "") {
        window.location.replace("login.html");
        return false;
    }

    return true;
}

async function fazerRequisicao(url, opcoes = {}) {
    const cabecalhos = {
        Authorization: `Bearer ${token}`,
        ...opcoes.headers
    };

    const resposta = await fetch(`${enderecoServidor}${url}`, {
        ...opcoes,
        headers: cabecalhos
    });

    if (resposta.status === 401 || resposta.status === 403) {
        localStorage.removeItem("token");
        window.location.replace("login.html");
        return null;
    }

    return resposta;
}

async function obterMensagemErro(resposta) {
    try {
        const dados = await resposta.json();

        if (typeof dados === "string") {
            return dados;
        }

        if (dados.message) {
            return dados.message;
        }

        return "Não foi possível realizar a operação.";
    } catch (erro) {
        return "Não foi possível realizar a operação.";
    }
}

function limparMensagens() {
    mensagemGrupo.textContent = "";
    mensagemProduto.textContent = "";
    mensagemMeta.textContent = "";
    mensagemLista.textContent = "";
}

function mostrarMensagem(elemento, mensagem, sucesso = false) {
    elemento.textContent = mensagem;
    elemento.style.color = sucesso ? "#16a34a" : "#dc2626";
}

async function carregarGrupos() {
    try {
        const resposta = await fazerRequisicao("/grupos");

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            const erro = await obterMensagemErro(resposta);
            mostrarMensagem(mensagemLista, erro);
            return;
        }

        const grupos = await resposta.json();

        grupoMeta.innerHTML = `
            <option value="">Selecione um grupo</option>
        `;

        grupos.forEach(function (grupo) {
            const opcao = document.createElement("option");

            opcao.value = grupo.id;
            opcao.textContent = grupo.nome;

            grupoMeta.appendChild(opcao);
        });

    } catch (erro) {
        console.error(erro);
        mostrarMensagem(
            mensagemLista,
            "Não foi possível carregar os grupos."
        );
    }
}

async function carregarProdutos() {
    try {
        const resposta = await fazerRequisicao("/produtos");

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            const erro = await obterMensagemErro(resposta);
            mostrarMensagem(mensagemLista, erro);
            return;
        }

        const produtos = await resposta.json();

        produtoMeta.innerHTML = `
            <option value="">Selecione um produto</option>
        `;

        produtos.forEach(function (produto) {
            const opcao = document.createElement("option");

            opcao.value = produto.id;
            opcao.textContent = `${produto.codigo} - ${produto.descricao}`;

            produtoMeta.appendChild(opcao);
        });

    } catch (erro) {
        console.error(erro);
        mostrarMensagem(
            mensagemLista,
            "Não foi possível carregar os produtos."
        );
    }
}

function escaparHtml(valor) {
    if (valor === null || valor === undefined) {
        return "";
    }

    return String(valor)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

async function carregarMetas() {
    try {
        const resposta = await fazerRequisicao("/metas");

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            const erro = await obterMensagemErro(resposta);
            mostrarMensagem(mensagemLista, erro);
            return;
        }

        const metas = await resposta.json();

        if (metas.length === 0) {
            listaMetas.innerHTML = `
                <tr>
                    <td colspan="4">
                        Nenhuma meta cadastrada.
                    </td>
                </tr>
            `;

            return;
        }

        listaMetas.innerHTML = "";

        metas.forEach(function (meta) {
            const linha = document.createElement("tr");

            linha.innerHTML = `
                <td>${escaparHtml(meta.grupoProducao?.nome)}</td>
                <td>${escaparHtml(meta.produto?.descricao)}</td>
                <td>${escaparHtml(meta.produto?.codigo)}</td>
                <td>${escaparHtml(meta.quantidadeMeta)}</td>
            `;

            listaMetas.appendChild(linha);
        });

    } catch (erro) {
        console.error(erro);

        listaMetas.innerHTML = `
            <tr>
                <td colspan="4">
                    Não foi possível carregar as metas.
                </td>
            </tr>
        `;
    }
}

async function carregarDados() {
    await carregarGrupos();
    await carregarProdutos();
    await carregarMetas();
}

formularioGrupo.addEventListener("submit", async function (evento) {
    evento.preventDefault();

    mensagemGrupo.textContent = "";

    const dados = {
        nome: nomeGrupo.value.trim()
    };

    try {
        const resposta = await fazerRequisicao("/grupos", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            const erro = await obterMensagemErro(resposta);
            mostrarMensagem(mensagemGrupo, erro);
            return;
        }

        await resposta.json();

        mostrarMensagem(
            mensagemGrupo,
            "Grupo cadastrado com sucesso.",
            true
        );

        formularioGrupo.reset();

        await carregarGrupos();

    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagemGrupo,
            "Não foi possível cadastrar o grupo."
        );
    }
});

formularioProduto.addEventListener("submit", async function (evento) {
    evento.preventDefault();

    mensagemProduto.textContent = "";

    const dados = {
        codigo: codigoProduto.value.trim(),
        descricao: descricaoProduto.value.trim()
    };

    try {
        const resposta = await fazerRequisicao("/produtos", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            const erro = await obterMensagemErro(resposta);
            mostrarMensagem(mensagemProduto, erro);
            return;
        }

        await resposta.json();

        mostrarMensagem(
            mensagemProduto,
            "Produto cadastrado com sucesso.",
            true
        );

        formularioProduto.reset();

        await carregarProdutos();

    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagemProduto,
            "Não foi possível cadastrar o produto."
        );
    }
});

formularioMeta.addEventListener("submit", async function (evento) {
    evento.preventDefault();

    mensagemMeta.textContent = "";

    const dados = {
        grupoProducaoId: Number(grupoMeta.value),
        produtoId: Number(produtoMeta.value),
        quantidadeMeta: Number(quantidadeMeta.value)
    };

    try {
        const resposta = await fazerRequisicao("/metas", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dados)
        });

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            const erro = await obterMensagemErro(resposta);
            mostrarMensagem(mensagemMeta, erro);
            return;
        }

        await resposta.json();

        mostrarMensagem(
            mensagemMeta,
            "Meta cadastrada com sucesso.",
            true
        );

        formularioMeta.reset();

        await carregarMetas();

    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagemMeta,
            "Não foi possível cadastrar a meta."
        );
    }
});

botaoAtualizar.addEventListener("click", async function () {
    mensagemLista.textContent = "";
    await carregarDados();
});

botaoSair.addEventListener("click", function () {
    localStorage.removeItem("token");
    window.location.replace("login.html");
});

if (verificarToken()) {
    carregarDados();
}