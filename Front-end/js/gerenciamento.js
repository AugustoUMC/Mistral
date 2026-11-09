const enderecoServidor = "http://localhost:8080";
const token = localStorage.getItem("token");

const formularioGrupo = document.getElementById("formularioGrupo");
const formularioProduto = document.getElementById("formularioProduto");
const formularioMeta = document.getElementById("formularioMeta");

const nomeGrupo = document.getElementById("nomeGrupo");
const codigoProduto = document.getElementById("codigoProduto");
const descricaoProduto = document.getElementById("descricaoProduto");
const precoBrutoProduto = document.getElementById("precoBrutoProduto");

const grupoMeta = document.getElementById("grupoMeta");
const produtoMeta = document.getElementById("produtoMeta");
const quantidadeMeta = document.getElementById("quantidadeMeta");

const mensagemGrupo = document.getElementById("mensagemGrupo");
const mensagemProduto = document.getElementById("mensagemProduto");
const mensagemMeta = document.getElementById("mensagemMeta");
const mensagemLista = document.getElementById("mensagemLista");

const listaGrupos = document.getElementById("listaGrupos");
const listaProdutos = document.getElementById("listaProdutos");
const listaMetas = document.getElementById("listaMetas");

const botaoAtualizar = document.getElementById("botaoAtualizar");
const botaoSair = document.getElementById("botaoSair");

const dialogPrecoProduto = document.getElementById("dialogPrecoProduto");
const formularioPrecoProduto = document.getElementById("formularioPrecoProduto");
const identificacaoProdutoPreco = document.getElementById("identificacaoProdutoPreco");
const precoBrutoEdicao = document.getElementById("precoBrutoEdicao");
const mensagemPrecoProduto = document.getElementById("mensagemPrecoProduto");
const botaoSalvarPreco = document.getElementById("botaoSalvarPreco");
const botaoFecharDialogPreco = document.getElementById("botaoFecharDialogPreco");
const botaoCancelarPreco = document.getElementById("botaoCancelarPreco");

let produtosCadastrados = [];
let produtoEmEdicaoPreco = null;


function verificarToken() {
    if (!token || token.trim() === "") {
        window.location.replace("login.html");
        return false;
    }

    return true;
}

async function fazerRequisicao(url, opcoes = {}) {
    const resposta = await fetch(`${enderecoServidor}${url}`, {
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

            if (dados.message || dados.erro || dados.detail) {
                return dados.message || dados.erro || dados.detail;
            }
        } catch (erro) {
            return texto;
        }
    }

    if (resposta.status === 409) {
        return "Não foi possível excluir porque existem registros vinculados a este item.";
    }

    return "Não foi possível realizar a operação.";
}

function mostrarMensagem(elemento, mensagem, sucesso = false) {
    elemento.textContent = mensagem;
    elemento.hidden = !mensagem;
    elemento.classList.toggle("sucesso", sucesso);
    elemento.classList.toggle("erro", Boolean(mensagem) && !sucesso);
}

function limparMensagens() {
    mostrarMensagem(mensagemGrupo, "");
    mostrarMensagem(mensagemProduto, "");
    mostrarMensagem(mensagemMeta, "");
    mostrarMensagem(mensagemLista, "");
    mostrarMensagem(mensagemPrecoProduto, "");
}

function criarCelula(valor) {
    const celula = document.createElement("td");

    celula.textContent =
        valor === null || valor === undefined ? "" : String(valor);

    return celula;
}

function formatarMoeda(valor) {
    if (valor === null || valor === undefined || valor === "") {
        return "Não informado";
    }

    return Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL",
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function obterPrecoNumerico(campo) {
    const valor = campo.value.trim();

    if (valor === "") {
        return null;
    }

    const numero = Number(valor);

    if (!Number.isFinite(numero) || numero < 0) {
        throw new Error(
            "Informe um valor unitário numérico maior ou igual a zero."
        );
    }

    return Number(numero.toFixed(2));
}

function criarBotaoAlterarPreco(produto) {
    const botao = document.createElement("button");

    botao.type = "button";
    botao.className = "botao-alterar-preco";
    botao.dataset.acao = "alterar-preco";
    botao.dataset.id = produto.id;
    botao.textContent = "Alterar valor";

    botao.setAttribute(
        "aria-label",
        `Alterar valor bruto unitário do produto ${produto.codigo}`
    );

    return botao;
}

function criarBotaoExcluir(tipo, id, rotulo) {
    const botao = document.createElement("button");

    botao.type = "button";
    botao.className = "botao-excluir";
    botao.dataset.tipo = tipo;
    botao.dataset.id = id;
    botao.textContent = "Excluir";

    botao.setAttribute(
        "aria-label",
        `Excluir ${rotulo}`
    );

    return botao;
}


async function carregarGrupos() {
    try {
        const resposta = await fazerRequisicao("/grupos");

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            mostrarMensagem(
                mensagemLista,
                await obterMensagemErro(resposta)
            );

            return;
        }

        const grupos = await resposta.json();

        grupoMeta.replaceChildren(
            new Option("Selecione um grupo", "")
        );

        listaGrupos.replaceChildren();

        if (!grupos.length) {
            listaGrupos.innerHTML =
                '<tr><td colspan="4">Nenhum grupo cadastrado.</td></tr>';

            return;
        }

        grupos.forEach(grupo => {
            grupoMeta.add(
                new Option(grupo.nome, grupo.id)
            );

            const linha = document.createElement("tr");

            linha.appendChild(criarCelula(grupo.id));
            linha.appendChild(criarCelula(grupo.nome));

            linha.appendChild(
                criarCelula(grupo.ativo ? "Ativo" : "Inativo")
            );

            const celulaAcoes = document.createElement("td");

            celulaAcoes.appendChild(
                criarBotaoExcluir(
                    "grupo",
                    grupo.id,
                    `grupo ${grupo.nome}`
                )
            );

            linha.appendChild(celulaAcoes);

            listaGrupos.appendChild(linha);
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
            mostrarMensagem(
                mensagemLista,
                await obterMensagemErro(resposta)
            );

            return;
        }

        const produtos = await resposta.json();

        produtosCadastrados = produtos;

        produtoMeta.replaceChildren(
            new Option("Selecione um produto", "")
        );

        listaProdutos.replaceChildren();

        if (!produtos.length) {
            listaProdutos.innerHTML =
                '<tr><td colspan="5">Nenhum produto cadastrado.</td></tr>';

            return;
        }

        produtos.forEach(produto => {
            produtoMeta.add(
                new Option(
                    `${produto.codigo} - ${produto.descricao}`,
                    produto.id
                )
            );

            const linha = document.createElement("tr");

            linha.appendChild(criarCelula(produto.id));
            linha.appendChild(criarCelula(produto.codigo));
            linha.appendChild(criarCelula(produto.descricao));

            linha.appendChild(
                criarCelula(formatarMoeda(produto.precoBruto))
            );

            const celulaAcoes = document.createElement("td");

            celulaAcoes.appendChild(
                criarBotaoAlterarPreco(produto)
            );

            celulaAcoes.appendChild(
                criarBotaoExcluir(
                    "produto",
                    produto.id,
                    `produto ${produto.codigo}`
                )
            );

            linha.appendChild(celulaAcoes);

            listaProdutos.appendChild(linha);
        });

    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagemLista,
            "Não foi possível carregar os produtos."
        );
    }
}


function abrirDialogPreco(produtoId) {
    const produto = produtosCadastrados.find(
        item => Number(item.id) === Number(produtoId)
    );

    if (!produto) {
        mostrarMensagem(
            mensagemLista,
            "Não foi possível localizar os dados do produto selecionado. Atualize a lista e tente novamente."
        );

        return;
    }

    produtoEmEdicaoPreco = produto;

    identificacaoProdutoPreco.textContent =
        `${produto.codigo} — ${produto.descricao}`;

    precoBrutoEdicao.value =
        produto.precoBruto === null ||
        produto.precoBruto === undefined
            ? ""
            : String(produto.precoBruto);

    mostrarMensagem(mensagemPrecoProduto, "");

    botaoSalvarPreco.disabled = false;

    if (typeof dialogPrecoProduto.showModal === "function") {
        dialogPrecoProduto.showModal();
    } else {
        dialogPrecoProduto.setAttribute("open", "");
    }
}

function fecharDialogPreco() {
    if (dialogPrecoProduto.open) {
        dialogPrecoProduto.close();
    } else {
        dialogPrecoProduto.removeAttribute("open");
    }

    produtoEmEdicaoPreco = null;
}

async function salvarPrecoProduto(evento) {
    evento.preventDefault();

    if (!produtoEmEdicaoPreco) {
        return;
    }

    if (!formularioPrecoProduto.reportValidity()) {
        return;
    }

    let novoPreco;

    try {
        novoPreco = obterPrecoNumerico(precoBrutoEdicao);

    } catch (erro) {
        mostrarMensagem(
            mensagemPrecoProduto,
            erro.message
        );

        return;
    }

    const precoAnterior = produtoEmEdicaoPreco.precoBruto;

    if (
        novoPreco === null &&
        precoAnterior !== null &&
        precoAnterior !== undefined
    ) {
        const confirmado = window.confirm(
            `Deseja remover o valor unitário do produto ${produtoEmEdicaoPreco.codigo}? Os relatórios deverão sinalizar que o valor não está informado.`
        );

        if (!confirmado) {
            return;
        }
    }

    const produto = produtoEmEdicaoPreco;

    const dadosAtualizados = {
        codigo: produto.codigo,
        descricao: produto.descricao,
        ean: produto.ean ?? null,
        dun: produto.dun ?? null,
        caixaMaster: produto.caixaMaster ?? null,
        diametroCm: produto.diametroCm ?? null,
        altura: produto.altura ?? null,
        precoBruto: novoPreco,
        imagem: produto.imagem ?? null
    };

    botaoSalvarPreco.disabled = true;

    mostrarMensagem(mensagemPrecoProduto, "");

    try {
        const resposta = await fazerRequisicao(
            `/produtos/${encodeURIComponent(produto.id)}`,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(dadosAtualizados)
            }
        );

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            mostrarMensagem(
                mensagemPrecoProduto,
                await obterMensagemErro(resposta)
            );

            return;
        }

        const produtoSalvo = await resposta.json();

        produtoEmEdicaoPreco = produtoSalvo;

        await carregarProdutos();
        await carregarMetas();

        mostrarMensagem(
            mensagemProduto,
            `Valor bruto unitário do produto ${produto.codigo} atualizado com sucesso.`,
            true
        );

        fecharDialogPreco();

    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagemPrecoProduto,
            "Não foi possível atualizar o valor unitário. Verifique a conexão com o servidor."
        );

    } finally {
        botaoSalvarPreco.disabled = false;
    }
}


async function carregarMetas() {
    try {
        const resposta = await fazerRequisicao("/metas");

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            mostrarMensagem(
                mensagemLista,
                await obterMensagemErro(resposta)
            );

            return;
        }

        const metas = await resposta.json();

        listaMetas.replaceChildren();

        if (!metas.length) {
            listaMetas.innerHTML =
                '<tr><td colspan="5">Nenhuma meta cadastrada.</td></tr>';

            return;
        }

        metas.forEach(meta => {
            const linha = document.createElement("tr");

            linha.appendChild(
                criarCelula(meta.grupoProducao?.nome)
            );

            linha.appendChild(
                criarCelula(meta.produto?.descricao)
            );

            linha.appendChild(
                criarCelula(meta.produto?.codigo)
            );

            linha.appendChild(
                criarCelula(meta.quantidadeMeta)
            );

            const celulaAcoes = document.createElement("td");

            celulaAcoes.appendChild(
                criarBotaoExcluir("meta", meta.id, "meta")
            );

            linha.appendChild(celulaAcoes);

            listaMetas.appendChild(linha);
        });

    } catch (erro) {
        console.error(erro);

        listaMetas.innerHTML =
            '<tr><td colspan="5">Não foi possível carregar as metas.</td></tr>';
    }
}


async function carregarDados() {
    mostrarMensagem(mensagemLista, "");

    await Promise.all([
        carregarGrupos(),
        carregarProdutos(),
        carregarMetas()
    ]);
}


async function excluirRegistro(tipo, id, botao) {
    const configuracoes = {
        grupo: {
            caminho: "/grupos/",
            nome: "este grupo"
        },
        produto: {
            caminho: "/produtos/",
            nome: "este produto"
        },
        meta: {
            caminho: "/metas/",
            nome: "esta meta"
        }
    };

    const configuracao = configuracoes[tipo];

    if (!configuracao) {
        return;
    }

    const confirmado = window.confirm(
        `Tem certeza de que deseja excluir ${configuracao.nome}? Esta ação não pode ser desfeita.`
    );

    if (!confirmado) {
        return;
    }

    botao.disabled = true;

    mostrarMensagem(mensagemLista, "");

    try {
        const resposta = await fazerRequisicao(
            `${configuracao.caminho}${encodeURIComponent(id)}`,
            {
                method: "DELETE"
            }
        );

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            mostrarMensagem(
                mensagemLista,
                await obterMensagemErro(resposta)
            );

            return;
        }

        await carregarDados();

        mostrarMensagem(
            mensagemLista,
            "Registro excluído com sucesso.",
            true
        );

    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagemLista,
            "Não foi possível excluir. Verifique se o backend está em execução e se existem registros vinculados."
        );

    } finally {
        botao.disabled = false;
    }
}


formularioGrupo.addEventListener("submit", async evento => {
    evento.preventDefault();

    mostrarMensagem(mensagemGrupo, "");

    try {
        const resposta = await fazerRequisicao("/grupos", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                nome: nomeGrupo.value.trim()
            })
        });

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            mostrarMensagem(
                mensagemGrupo,
                await obterMensagemErro(resposta)
            );

            return;
        }

        mostrarMensagem(
            mensagemGrupo,
            "Grupo cadastrado com sucesso.",
            true
        );

        formularioGrupo.reset();

        await carregarDados();

    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagemGrupo,
            "Não foi possível cadastrar o grupo."
        );
    }
});


formularioProduto.addEventListener("submit", async evento => {
    evento.preventDefault();

    mostrarMensagem(mensagemProduto, "");

    try {
        const resposta = await fazerRequisicao("/produtos", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                codigo: codigoProduto.value.trim(),
                descricao: descricaoProduto.value.trim(),
                precoBruto: obterPrecoNumerico(precoBrutoProduto)
            })
        });

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            mostrarMensagem(
                mensagemProduto,
                await obterMensagemErro(resposta)
            );

            return;
        }

        mostrarMensagem(
            mensagemProduto,
            "Produto cadastrado com sucesso.",
            true
        );

        formularioProduto.reset();

        await carregarDados();

    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagemProduto,
            "Não foi possível cadastrar o produto."
        );
    }
});


formularioMeta.addEventListener("submit", async evento => {
    evento.preventDefault();

    mostrarMensagem(mensagemMeta, "");

    try {
        const resposta = await fazerRequisicao("/metas", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                grupoProducaoId: Number(grupoMeta.value),
                produtoId: Number(produtoMeta.value),
                quantidadeMeta: Number(quantidadeMeta.value)
            })
        });

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            mostrarMensagem(
                mensagemMeta,
                await obterMensagemErro(resposta)
            );

            return;
        }

        mostrarMensagem(
            mensagemMeta,
            "Meta cadastrada com sucesso.",
            true
        );

        formularioMeta.reset();

        await carregarDados();

    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagemMeta,
            "Não foi possível cadastrar a meta."
        );
    }
});


listaProdutos.addEventListener("click", evento => {
    const botao = evento.target.closest(
        'button[data-acao="alterar-preco"][data-id]'
    );

    if (!botao) {
        return;
    }

    abrirDialogPreco(botao.dataset.id);
});


formularioPrecoProduto.addEventListener(
    "submit",
    salvarPrecoProduto
);

botaoFecharDialogPreco.addEventListener(
    "click",
    fecharDialogPreco
);

botaoCancelarPreco.addEventListener(
    "click",
    fecharDialogPreco
);

dialogPrecoProduto.addEventListener("click", evento => {
    if (evento.target === dialogPrecoProduto) {
        fecharDialogPreco();
    }
});

dialogPrecoProduto.addEventListener("close", () => {
    produtoEmEdicaoPreco = null;
});


[listaGrupos, listaProdutos, listaMetas].forEach(tabela => {
    tabela.addEventListener("click", evento => {
        const botao = evento.target.closest(
            "button[data-tipo][data-id]"
        );

        if (!botao) {
            return;
        }

        excluirRegistro(
            botao.dataset.tipo,
            botao.dataset.id,
            botao
        );
    });
});


botaoAtualizar.addEventListener(
    "click",
    carregarDados
);

botaoSair.addEventListener("click", () => {
    localStorage.removeItem("token");
    window.location.replace("login.html");
});


if (verificarToken()) {
    carregarDados();
}