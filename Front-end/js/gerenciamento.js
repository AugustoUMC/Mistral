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
const dialogEdicao = document.getElementById("dialogEdicaoGerenciamento");
const formularioEdicao = document.getElementById("formularioEdicaoGerenciamento");
const camposEdicao = document.getElementById("camposEdicaoGerenciamento");
const mensagemEdicao = document.getElementById("mensagemEdicaoGerenciamento");
const botaoSalvarEdicao = document.getElementById("salvarEdicaoGerenciamento");

let registroEmEdicao = null;
let tipoEmEdicao = null;
let salvandoEdicao = false;

const recursos = {
    grupo: "/grupos",
    produto: "/produtos",
    meta: "/metas"
};

const camposPorTipo = {
    grupo: [
        {
            nome: "nome",
            rotulo: "Nome do grupo",
            tipo: "text",
            obrigatorio: true
        },
        {
            nome: "ativo",
            rotulo: "Situação",
            tipo: "select",
            opcoes: [
                { valor: "true", texto: "Ativo" },
                { valor: "false", texto: "Inativo" }
            ]
        }
    ],
    produto: [
        {
            nome: "codigo",
            rotulo: "Código",
            tipo: "text",
            obrigatorio: true
        },
        {
            nome: "descricao",
            rotulo: "Descrição",
            tipo: "text",
            obrigatorio: true
        },
        {
            nome: "ean",
            rotulo: "EAN",
            tipo: "text"
        },
        {
            nome: "dun",
            rotulo: "DUN",
            tipo: "text"
        },
        {
            nome: "caixaMaster",
            rotulo: "Caixa master",
            tipo: "number",
            passo: "1",
            minimo: "0"
        },
        {
            nome: "diametroCm",
            rotulo: "Diâmetro (cm)",
            tipo: "number",
            passo: "any",
            minimo: "0"
        },
        {
            nome: "altura",
            rotulo: "Altura",
            tipo: "number",
            passo: "any",
            minimo: "0"
        },
        {
            nome: "precoBruto",
            rotulo: "Valor bruto unitário (R$)",
            tipo: "number",
            passo: "0.01",
            minimo: "0"
        },
        {
            nome: "imagem",
            rotulo: "Imagem (referência/URL)",
            tipo: "text"
        }
    ],
    meta: [
        {
            nome: "grupoProducaoId",
            rotulo: "Grupo",
            tipo: "select",
            obrigatorio: true
        },
        {
            nome: "produtoId",
            rotulo: "Produto",
            tipo: "select",
            obrigatorio: true
        },
        {
            nome: "quantidadeMeta",
            rotulo: "Quantidade da meta",
            tipo: "number",
            passo: "1",
            minimo: "1",
            obrigatorio: true
        }
    ]
};

function verificarToken() {
    if (!token || !token.trim()) {
        window.location.replace("login.html");
        return false;
    }

    return true;
}

async function fazerRequisicao(caminho, opcoes = {}) {
    const resposta = await fetch(`${enderecoServidor}${caminho}`, {
        ...opcoes,
        headers: {
            ...opcoes.headers,
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
        return "Não foi possível concluir a operação porque existem registros vinculados.";
    }

    return "Não foi possível realizar a operação.";
}

function mostrarMensagem(elemento, texto, sucesso = false) {
    elemento.textContent = texto;
    elemento.hidden = !texto;
    elemento.classList.toggle("sucesso", sucesso);
    elemento.classList.toggle("erro", Boolean(texto) && !sucesso);
}

function criarCelula(valor) {
    const celula = document.createElement("td");

    celula.textContent =
        valor === null || valor === undefined
            ? ""
            : String(valor);

    return celula;
}

function formatarMoeda(valor) {
    if (valor === null || valor === undefined || valor === "") {
        return "Não informado";
    }

    return Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

function obterPrecoNumerico(campo) {
    if (!campo.value.trim()) {
        return null;
    }

    const numero = Number(campo.value);

    if (
        !Number.isFinite(numero) ||
        numero < 0 ||
        Math.abs(numero * 100 - Math.round(numero * 100)) > 0.000001
    ) {
        throw new Error(
            "Informe um valor unitário maior ou igual a zero, com no máximo duas casas decimais."
        );
    }

    return Number(numero.toFixed(2));
}

function criarBotao(acao, tipo, id, rotulo) {
    const botao = document.createElement("button");

    botao.type = "button";
    botao.className =
        acao === "editar" ? "botao-editar" : "botao-excluir";

    botao.dataset.acao = acao;
    botao.dataset.tipo = tipo;
    botao.dataset.id = id;

    botao.textContent =
        acao === "editar" ? "Editar" : "Excluir";

    botao.setAttribute(
        "aria-label",
        `${botao.textContent} ${rotulo}`
    );

    return botao;
}

function mostrarLinhaVazia(tabela, colunas, texto) {
    const linha = document.createElement("tr");
    const celula = criarCelula(texto);

    celula.colSpan = colunas;
    linha.appendChild(celula);
    tabela.replaceChildren(linha);
}

async function carregarGrupos() {
    const resposta = await fazerRequisicao("/grupos");

    if (!resposta) {
        return;
    }

    if (!resposta.ok) {
        throw new Error(await obterMensagemErro(resposta));
    }

    const grupos = await resposta.json();

    grupoMeta.replaceChildren(
        new Option("Selecione um grupo", "")
    );

    listaGrupos.replaceChildren();

    if (!grupos.length) {
        mostrarLinhaVazia(
            listaGrupos,
            4,
            "Nenhum grupo cadastrado."
        );

        return;
    }

    for (const grupo of grupos) {
        grupoMeta.add(
            new Option(grupo.nome, grupo.id)
        );

        const linha = document.createElement("tr");

        linha.append(
            criarCelula(grupo.id),
            criarCelula(grupo.nome),
            criarCelula(grupo.ativo ? "Ativo" : "Inativo")
        );

        const acoes = document.createElement("td");

        acoes.append(
            criarBotao("editar", "grupo", grupo.id, grupo.nome),
            criarBotao("excluir", "grupo", grupo.id, grupo.nome)
        );

        linha.append(acoes);
        listaGrupos.appendChild(linha);
    }
}

async function carregarProdutos() {
    const resposta = await fazerRequisicao("/produtos");

    if (!resposta) {
        return;
    }

    if (!resposta.ok) {
        throw new Error(await obterMensagemErro(resposta));
    }

    const produtos = await resposta.json();

    produtoMeta.replaceChildren(
        new Option("Selecione um produto", "")
    );

    listaProdutos.replaceChildren();

    if (!produtos.length) {
        mostrarLinhaVazia(
            listaProdutos,
            5,
            "Nenhum produto cadastrado."
        );

        return;
    }

    for (const produto of produtos) {
        produtoMeta.add(
            new Option(
                `${produto.codigo} - ${produto.descricao}`,
                produto.id
            )
        );

        const linha = document.createElement("tr");

        linha.append(
            criarCelula(produto.id),
            criarCelula(produto.codigo),
            criarCelula(produto.descricao),
            criarCelula(formatarMoeda(produto.precoBruto))
        );

        const acoes = document.createElement("td");

        acoes.append(
            criarBotao(
                "editar",
                "produto",
                produto.id,
                produto.codigo
            ),
            criarBotao(
                "excluir",
                "produto",
                produto.id,
                produto.codigo
            )
        );

        linha.append(acoes);
        listaProdutos.appendChild(linha);
    }
}

async function carregarMetas() {
    const resposta = await fazerRequisicao("/metas");

    if (!resposta) {
        return;
    }

    if (!resposta.ok) {
        throw new Error(await obterMensagemErro(resposta));
    }

    const metas = await resposta.json();

    listaMetas.replaceChildren();

    if (!metas.length) {
        mostrarLinhaVazia(
            listaMetas,
            5,
            "Nenhuma meta cadastrada."
        );

        return;
    }

    for (const meta of metas) {
        const linha = document.createElement("tr");

        linha.append(
            criarCelula(meta.grupoProducao?.nome),
            criarCelula(meta.produto?.descricao),
            criarCelula(meta.produto?.codigo),
            criarCelula(meta.quantidadeMeta)
        );

        const acoes = document.createElement("td");

        acoes.append(
            criarBotao(
                "editar",
                "meta",
                meta.id,
                "meta"
            ),
            criarBotao(
                "excluir",
                "meta",
                meta.id,
                "meta"
            )
        );

        linha.append(acoes);
        listaMetas.appendChild(linha);
    }
}

async function carregarDados() {
    mostrarMensagem(mensagemLista, "");

    const resultados = await Promise.allSettled([
        carregarGrupos(),
        carregarProdutos(),
        carregarMetas()
    ]);

    const falhas = resultados.filter(
        resultado => resultado.status === "rejected"
    );

    if (falhas.length) {
        console.error(
            falhas.map(resultado => resultado.reason)
        );

        mostrarMensagem(
            mensagemLista,
            falhas[0].reason?.message ||
                "Não foi possível carregar todos os registros."
        );
    }
}

async function enviarDados(
    caminho,
    metodo,
    dados,
    mensagemElemento,
    formulario
) {
    const resposta = await fazerRequisicao(caminho, {
        method: metodo,
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(dados)
    });

    if (!resposta) {
        return false;
    }

    if (!resposta.ok) {
        throw new Error(
            await obterMensagemErro(resposta)
        );
    }

    if (formulario) {
        formulario.reset();
    }

    await carregarDados();

    mostrarMensagem(
        mensagemElemento,
        "Operação realizada com sucesso.",
        true
    );

    return true;
}

async function executarCadastro(
    evento,
    caminho,
    dados,
    mensagem,
    formulario
) {
    evento.preventDefault();

    mostrarMensagem(mensagem, "");

    try {
        await enviarDados(
            caminho,
            "POST",
            dados(),
            mensagem,
            formulario
        );
    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagem,
            erro.message || "Não foi possível cadastrar."
        );
    }
}

async function excluirRegistro(tipo, id, botao) {
    const descricoes = {
        grupo: "este grupo",
        produto: "este produto",
        meta: "esta meta"
    };

    if (
        !window.confirm(
            `Tem certeza de que deseja excluir ${descricoes[tipo]}? Esta ação não pode ser desfeita.`
        )
    ) {
        return;
    }

    botao.disabled = true;
    mostrarMensagem(mensagemLista, "");

    try {
        const resposta = await fazerRequisicao(
            `${recursos[tipo]}/${encodeURIComponent(id)}`,
            {
                method: "DELETE"
            }
        );

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            throw new Error(
                await obterMensagemErro(resposta)
            );
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
            erro.message ||
                "Não foi possível excluir o registro."
        );
    } finally {
        botao.disabled = false;
    }
}

function fecharDialog(dialogo) {
    if (dialogo.open && typeof dialogo.close === "function") {
        dialogo.close();
    } else {
        dialogo.removeAttribute("open");
    }
}

function abrirDialog(dialogo) {
    if (typeof dialogo.showModal === "function") {
        dialogo.showModal();
    } else {
        dialogo.setAttribute("open", "");
    }
}

function criarCampo(definicao, valor, opcoes = []) {
    const recipiente = document.createElement("div");
    recipiente.className = "campo";

    const rotulo = document.createElement("label");
    rotulo.htmlFor = `edicao-${definicao.nome}`;
    rotulo.textContent = definicao.rotulo;

    const campo = document.createElement(
        definicao.tipo === "select" ? "select" : "input"
    );

    campo.id = rotulo.htmlFor;
    campo.name = definicao.nome;
    campo.required = Boolean(definicao.obrigatorio);

    if (definicao.tipo === "select") {
        for (const opcao of definicao.opcoes || opcoes) {
            campo.add(
                new Option(opcao.texto, opcao.valor)
            );
        }
    } else {
        campo.type = definicao.tipo;

        if (definicao.passo) {
            campo.step = definicao.passo;
        }

        if (definicao.minimo) {
            campo.min = definicao.minimo;
        }
    }

    campo.value = valor ?? "";

    recipiente.append(rotulo, campo);

    return recipiente;
}

async function editarRegistro(tipo, id) {
    tipoEmEdicao = tipo;
    registroEmEdicao = null;

    camposEdicao.replaceChildren();
    mostrarMensagem(mensagemEdicao, "");

    botaoSalvarEdicao.disabled = true;

    document.getElementById(
        "tituloDialogEdicao"
    ).textContent = `Editar ${
        {
            grupo: "grupo",
            produto: "produto",
            meta: "meta"
        }[tipo]
    }`;

    abrirDialog(dialogEdicao);

    try {
        const resposta = await fazerRequisicao(
            `${recursos[tipo]}/${encodeURIComponent(id)}`
        );

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            throw new Error(
                await obterMensagemErro(resposta)
            );
        }

        const item = await resposta.json();
        registroEmEdicao = item;

        let opcoesGrupo = [];
        let opcoesProduto = [];

        if (tipo === "meta") {
            const [
                respostaGrupos,
                respostaProdutos
            ] = await Promise.all([
                fazerRequisicao("/grupos"),
                fazerRequisicao("/produtos")
            ]);

            if (!respostaGrupos || !respostaProdutos) {
                return;
            }

            if (!respostaGrupos.ok) {
                throw new Error(
                    await obterMensagemErro(respostaGrupos)
                );
            }

            if (!respostaProdutos.ok) {
                throw new Error(
                    await obterMensagemErro(respostaProdutos)
                );
            }

            opcoesGrupo = (
                await respostaGrupos.json()
            ).map(item => ({
                valor: String(item.id),
                texto: item.nome
            }));

            opcoesProduto = (
                await respostaProdutos.json()
            ).map(item => ({
                valor: String(item.id),
                texto: `${item.codigo} - ${item.descricao}`
            }));
        }

        for (const definicao of camposPorTipo[tipo]) {
            let valor = item[definicao.nome];
            let opcoes = [];

            if (
                tipo === "meta" &&
                definicao.nome === "grupoProducaoId"
            ) {
                valor = item.grupoProducao?.id;
                opcoes = opcoesGrupo;
            }

            if (
                tipo === "meta" &&
                definicao.nome === "produtoId"
            ) {
                valor = item.produto?.id;
                opcoes = opcoesProduto;
            }

            camposEdicao.appendChild(
                criarCampo(
                    definicao,
                    valor,
                    opcoes
                )
            );
        }

        botaoSalvarEdicao.disabled = false;
    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagemEdicao,
            erro.message ||
                "Não foi possível carregar os dados para edição."
        );
    }
}

function lerDadosEdicao() {
    const dados = {};

    for (const definicao of camposPorTipo[tipoEmEdicao]) {
        const valor = formularioEdicao.elements
            .namedItem(definicao.nome)
            .value.trim();

        if (definicao.tipo === "number") {
            dados[definicao.nome] =
                valor === "" ? null : Number(valor);

            if (
                valor !== "" &&
                !Number.isFinite(dados[definicao.nome])
            ) {
                throw new Error(
                    `Valor inválido: ${definicao.rotulo}.`
                );
            }
        } else if (definicao.nome === "ativo") {
            dados.ativo = valor === "true";
        } else if (
            tipoEmEdicao === "meta" &&
            definicao.tipo === "select"
        ) {
            dados[definicao.nome] = Number(valor);
        } else {
            dados[definicao.nome] =
                valor ||
                (definicao.obrigatorio ? "" : null);
        }
    }

    if (
        tipoEmEdicao === "produto" &&
        dados.precoBruto !== null
    ) {
        if (
            dados.precoBruto < 0 ||
            Math.abs(
                dados.precoBruto * 100 -
                Math.round(dados.precoBruto * 100)
            ) > 0.000001
        ) {
            throw new Error(
                "O valor bruto unitário deve ter no máximo duas casas decimais."
            );
        }
    }

    return dados;
}

async function salvarEdicao(evento) {
    evento.preventDefault();

    if (
        !registroEmEdicao ||
        salvandoEdicao ||
        !formularioEdicao.reportValidity()
    ) {
        return;
    }

    let dados;

    try {
        dados = lerDadosEdicao();
    } catch (erro) {
        mostrarMensagem(
            mensagemEdicao,
            erro.message
        );

        return;
    }

    salvandoEdicao = true;
    botaoSalvarEdicao.disabled = true;

    mostrarMensagem(mensagemEdicao, "");

    try {
        const tipo = tipoEmEdicao;

        const resposta = await fazerRequisicao(
            `${recursos[tipo]}/${encodeURIComponent(registroEmEdicao.id)}`,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(dados)
            }
        );

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            throw new Error(
                await obterMensagemErro(resposta)
            );
        }

        fecharDialog(dialogEdicao);

        registroEmEdicao = null;

        await carregarDados();

        mostrarMensagem(
            {
                grupo: mensagemGrupo,
                produto: mensagemProduto,
                meta: mensagemMeta
            }[tipo],
            "Registro atualizado com sucesso.",
            true
        );
    } catch (erro) {
        console.error(erro);

        mostrarMensagem(
            mensagemEdicao,
            erro.message ||
                "Não foi possível salvar as alterações."
        );
    } finally {
        salvandoEdicao = false;
        botaoSalvarEdicao.disabled = false;
    }
}

formularioGrupo.addEventListener(
    "submit",
    evento => executarCadastro(
        evento,
        "/grupos",
        () => ({
            nome: nomeGrupo.value.trim()
        }),
        mensagemGrupo,
        formularioGrupo
    )
);

formularioProduto.addEventListener(
    "submit",
    evento => executarCadastro(
        evento,
        "/produtos",
        () => ({
            codigo: codigoProduto.value.trim(),
            descricao: descricaoProduto.value.trim(),
            precoBruto: obterPrecoNumerico(
                precoBrutoProduto
            )
        }),
        mensagemProduto,
        formularioProduto
    )
);

formularioMeta.addEventListener(
    "submit",
    evento => executarCadastro(
        evento,
        "/metas",
        () => ({
            grupoProducaoId: Number(grupoMeta.value),
            produtoId: Number(produtoMeta.value),
            quantidadeMeta: Number(quantidadeMeta.value)
        }),
        mensagemMeta,
        formularioMeta
    )
);

formularioEdicao.addEventListener(
    "submit",
    salvarEdicao
);

[listaGrupos, listaProdutos, listaMetas].forEach(
    tabela => {
        tabela.addEventListener(
            "click",
            evento => {
                const botao = evento.target.closest(
                    "button[data-acao][data-id][data-tipo]"
                );

                if (!botao) {
                    return;
                }

                const {
                    acao,
                    tipo,
                    id
                } = botao.dataset;

                if (acao === "editar") {
                    editarRegistro(tipo, id);
                }

                if (acao === "excluir") {
                    excluirRegistro(tipo, id, botao);
                }
            }
        );
    }
);

document.getElementById(
    "fecharEdicaoGerenciamento"
).addEventListener(
    "click",
    () => {
        if (!salvandoEdicao) {
            fecharDialog(dialogEdicao);
        }
    }
);

document.getElementById(
    "cancelarEdicaoGerenciamento"
).addEventListener(
    "click",
    () => {
        if (!salvandoEdicao) {
            fecharDialog(dialogEdicao);
        }
    }
);

dialogEdicao.addEventListener(
    "click",
    evento => {
        if (
            evento.target === dialogEdicao &&
            !salvandoEdicao
        ) {
            fecharDialog(dialogEdicao);
        }
    }
);

dialogEdicao.addEventListener(
    "cancel",
    evento => {
        if (salvandoEdicao) {
            evento.preventDefault();
        }
    }
);

botaoAtualizar.addEventListener(
    "click",
    carregarDados
);

botaoSair.addEventListener(
    "click",
    () => {
        localStorage.removeItem("token");
        window.location.replace("login.html");
    }
);

if (verificarToken()) {
    carregarDados();
}
