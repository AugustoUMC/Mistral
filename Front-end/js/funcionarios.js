const enderecoServidor = "http://localhost:8080";

const formulario = document.getElementById("formularioFuncionario");
const campoAcesso = document.getElementById("acessoSistema");
const campoSenha = document.getElementById("senha");
const campoConfirmarSenha = document.getElementById("confirmarSenha");

const containerSenha = document.getElementById("campoSenha");
const containerConfirmarSenha = document.getElementById("campoConfirmarSenha");

const painelFuncionarios = document.getElementById("painelFuncionarios");
const avisoRestrito = document.getElementById("avisoRestrito");

const mensagemCadastro = document.getElementById("mensagemCadastro");
const mensagemLista = document.getElementById("mensagemLista");
const resultadoSelecao = document.getElementById("resultadoSelecao");
const listaFuncionarios = document.getElementById("listaFuncionarios");


const dialogGruposFuncionario =
    document.getElementById("dialogGruposFuncionario");

const nomeFuncionarioGrupos =
    document.getElementById("nomeFuncionarioGrupos");

const ajudaDialogGrupos =
    document.getElementById("ajudaDialogGrupos");

const listaGruposDisponiveis =
    document.getElementById("listaGruposDisponiveis");

const historicoGrupos =
    document.getElementById("historicoGrupos");

const mensagemGrupos =
    document.getElementById("mensagemGrupos");

const botaoSalvarGrupos =
    document.getElementById("botaoSalvarGrupos");

const dialogEnderecoFuncionario =
    document.getElementById("dialogEnderecoFuncionario");

const nomeFuncionarioEndereco =
    document.getElementById("nomeFuncionarioEndereco");

const mensagemEndereco =
    document.getElementById("mensagemEndereco");

const botaoSalvarEndereco =
    document.getElementById("botaoSalvarEndereco");

let funcionarios = [];

let funcionarioEmEdicaoGrupos = null;
let funcionarioEmEdicaoEndereco = null;
let gruposCadastrados = [];
let alocacoesFuncionario = [];


function obterToken() {
    return localStorage.getItem("token");
}

function exibirMensagem(elemento, texto, tipo = "erro") {
    elemento.textContent = texto;
    elemento.className = texto ? `mensagem ${tipo}` : "mensagem";
}

function formatarMoeda(valor) {
    return Number(valor || 0).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

function formatarData(data) {
    if (!data) {
        return "—";
    }

    const partes = String(data).slice(0, 10).split("-");

    if (partes.length !== 3) {
        return String(data);
    }

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

function decodificarToken() {
    try {
        const token = obterToken();

        if (!token) {
            return null;
        }

        const partes = token.split(".");

        if (partes.length !== 3) {
            return null;
        }

        const payload = partes[1]
            .replace(/-/g, "+")
            .replace(/_/g, "/");

        const preenchido = payload.padEnd(
            payload.length + ((4 - payload.length % 4) % 4),
            "="
        );

        return JSON.parse(atob(preenchido));

    } catch {
        return null;
    }
}

function verificarPermissaoAdmin() {
    const token = obterToken();

    if (!token) {
        window.location.replace("login.html");
        return false;
    }

    const dados = decodificarToken();

    if (
        !dados ||
        !dados.exp ||
        dados.exp <= Math.floor(Date.now() / 1000)
    ) {
        localStorage.removeItem("token");
        window.location.replace("login.html");
        return false;
    }

    if (String(dados.perfil || "").toUpperCase() !== "ADMIN") {
        avisoRestrito.hidden = false;
        painelFuncionarios.hidden = true;
        return false;
    }

    painelFuncionarios.hidden = false;
    avisoRestrito.hidden = true;

    return true;
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

            return dados.erro ||
                dados.message ||
                dados.detail ||
                dados.title ||
                "Não foi possível concluir a operação.";

        } catch {
            return texto;
        }
    }

    if (resposta.status === 404) {
        return "O funcionário não foi encontrado.";
    }

    if (resposta.status === 409) {
        return "A operação não pôde ser concluída devido a um conflito com os dados.";
    }

    return "Não foi possível concluir a operação.";
}


function configurarCampoSenha() {
    const habilitarSenha = campoAcesso.value === "true";

    containerSenha.hidden = !habilitarSenha;
    containerConfirmarSenha.hidden = !habilitarSenha;

    campoSenha.disabled = !habilitarSenha;
    campoConfirmarSenha.disabled = !habilitarSenha;

    campoSenha.required = habilitarSenha;
    campoConfirmarSenha.required = habilitarSenha;

    document.querySelectorAll(".botao-olho").forEach(botao => {
        botao.disabled = !habilitarSenha;

        if (!habilitarSenha) {
            const campo = document.getElementById(botao.dataset.alvo);

            const descricao = botao.dataset.alvo === "confirmarSenha"
                ? "confirmação da senha"
                : "senha";

            campo.type = "password";

            botao.setAttribute("aria-pressed", "false");
            botao.setAttribute("aria-label", `Mostrar ${descricao}`);
            botao.setAttribute("title", `Mostrar ${descricao}`);

            const risco = botao.querySelector(".risco-olho");

            if (risco) {
                risco.style.display = "none";
            }
        }
    });

    if (!habilitarSenha) {
        campoSenha.value = "";
        campoConfirmarSenha.value = "";
    }
}

function configurarOlhos() {
    document.querySelectorAll(".botao-olho").forEach(botao => {
        botao.addEventListener("click", () => {
            const campo = document.getElementById(botao.dataset.alvo);

            if (!campo || campo.disabled) {
                return;
            }

            const mostrar = campo.type === "password";

            const descricao = botao.dataset.alvo === "confirmarSenha"
                ? "confirmação da senha"
                : "senha";

            campo.type = mostrar ? "text" : "password";

            botao.setAttribute(
                "aria-pressed",
                String(mostrar)
            );

            botao.setAttribute(
                "aria-label",
                `${mostrar ? "Ocultar" : "Mostrar"} ${descricao}`
            );

            botao.setAttribute(
                "title",
                `${mostrar ? "Ocultar" : "Mostrar"} ${descricao}`
            );

            const risco = botao.querySelector(".risco-olho");

            if (risco) {
                risco.style.display = mostrar ? "block" : "none";
            }
        });
    });
}


function criarCelula(valor) {
    const celula = document.createElement("td");

    celula.textContent =
        valor === null || valor === undefined ? "" : String(valor);

    return celula;
}

async function carregarFuncionarios() {
    exibirMensagem(mensagemLista, "");
    resultadoSelecao.textContent = "";

    listaFuncionarios.innerHTML =
        '<tr><td colspan="10">Carregando funcionários...</td></tr>';

    try {
        const resposta = await requisitarApi("/relatorios/funcionarios");

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            exibirMensagem(
                mensagemLista,
                await obterMensagemErro(resposta)
            );

            listaFuncionarios.innerHTML =
                '<tr><td colspan="10">Não foi possível carregar a lista.</td></tr>';

            return;
        }

        funcionarios = await resposta.json();

        renderizarFuncionarios();

        await carregarResumo();

    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemLista,
            "Não foi possível conectar ao servidor."
        );

        listaFuncionarios.innerHTML =
            '<tr><td colspan="10">Falha na comunicação com o backend.</td></tr>';
    }
}

async function carregarResumo() {
    try {
        const resposta = await requisitarApi(
            "/relatorios/funcionarios/soma-salarios"
        );

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            exibirMensagem(
                mensagemLista,
                await obterMensagemErro(resposta)
            );

            return;
        }

        const dados = await resposta.json();

        document.getElementById("totalFuncionarios").textContent =
            dados.quantidadeFuncionarios ?? funcionarios.length;

        document.getElementById("somaSalarios").textContent =
            formatarMoeda(dados.somaSalarios);

        document.getElementById("totalAtivos").textContent =
            funcionarios.filter(
                funcionario => funcionario.ativo === true
            ).length;

    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemLista,
            "Não foi possível carregar o resumo salarial."
        );
    }
}

function renderizarFuncionarios() {
    listaFuncionarios.replaceChildren();

    if (!funcionarios.length) {
        listaFuncionarios.innerHTML =
            '<tr><td colspan="10">Nenhum funcionário cadastrado.</td></tr>';

        return;
    }

    funcionarios.forEach(funcionario => {
        const linha = document.createElement("tr");

        const celulaSelecao = document.createElement("td");
        const checkbox = document.createElement("input");

        checkbox.type = "checkbox";
        checkbox.className = "funcionarioSelecionado";
        checkbox.value = funcionario.matricula;
        checkbox.disabled = funcionario.ativo !== true;

        checkbox.setAttribute(
            "aria-label",
            `Selecionar ${funcionario.nome}`
        );

        celulaSelecao.appendChild(checkbox);
        linha.appendChild(celulaSelecao);

        linha.appendChild(criarCelula(funcionario.nome));
        linha.appendChild(criarCelula(funcionario.matricula));
        linha.appendChild(criarCelula(funcionario.cargo));

        const gruposAtuais = Array.isArray(funcionario.gruposAtuais)
            ? funcionario.gruposAtuais
            : [];

        linha.appendChild(criarCelula(
            gruposAtuais.length
                ? gruposAtuais.join(", ")
                : "Sem grupo"
        ));

        linha.appendChild(criarCelula(
            formatarMoeda(funcionario.salario)
        ));

        linha.appendChild(criarCelula(
            funcionario.ativo ? "Sim" : "Não"
        ));

        linha.appendChild(criarCelula(
            funcionario.acessoSistema ? "Sim" : "Não"
        ));

        linha.appendChild(criarCelula(funcionario.perfil));

        const celulaAcao = document.createElement("td");
        celulaAcao.className = "acoes-funcionario";

        const botaoEndereco = document.createElement("button");

        botaoEndereco.type = "button";
        botaoEndereco.className = "botao-endereco";
        botaoEndereco.textContent = "Dados pessoais";
        botaoEndereco.dataset.acao = "endereco";
        botaoEndereco.dataset.id = funcionario.id;
        botaoEndereco.dataset.nome = funcionario.nome;

        botaoEndereco.setAttribute(
            "aria-label",
            `Consultar ou atualizar os dados pessoais de ${funcionario.nome}`
        );

        celulaAcao.appendChild(botaoEndereco);

        const botaoGrupos = document.createElement("button");

        botaoGrupos.type = "button";
        botaoGrupos.className = "botao-grupos";
        botaoGrupos.textContent = "Gerenciar grupos";
        botaoGrupos.dataset.acao = "grupos";
        botaoGrupos.dataset.id = funcionario.id;
        botaoGrupos.dataset.nome = funcionario.nome;
        botaoGrupos.dataset.ativo = String(
            funcionario.ativo === true
        );

        botaoGrupos.setAttribute(
            "aria-label",
            `Gerenciar grupos do funcionário ${funcionario.nome}`
        );

        celulaAcao.appendChild(botaoGrupos);

        if (funcionario.ativo) {

            const botaoInativar = document.createElement("button");

            botaoInativar.type = "button";
            botaoInativar.className = "botao-inativar";
            botaoInativar.textContent = "Inativar";
            botaoInativar.dataset.acao = "inativar";
            botaoInativar.dataset.id = funcionario.id;
            botaoInativar.dataset.nome = funcionario.nome;

            botaoInativar.setAttribute(
                "aria-label",
                `Inativar funcionário ${funcionario.nome}`
            );

            celulaAcao.appendChild(botaoInativar);

        } else {

            const estado = document.createElement("span");

            estado.className = "estado-inativo";
            estado.textContent = "Inativo";

            celulaAcao.appendChild(estado);
        }

        linha.appendChild(celulaAcao);

        listaFuncionarios.appendChild(linha);
    });
}

function renderizarOpcoesGrupos() {
    listaGruposDisponiveis.replaceChildren();

    if (
        funcionarioEmEdicaoGrupos &&
        funcionarioEmEdicaoGrupos.ativo !== true
    ) {
        const aviso = document.createElement("p");

        aviso.textContent =
            "Funcionário inativo. Não é possível associar novos grupos.";

        listaGruposDisponiveis.appendChild(aviso);

        return;
    }

    const gruposAtuais = new Set(
        alocacoesFuncionario
            .filter(alocacao => alocacao.atual === true)
            .map(alocacao => Number(alocacao.grupoId))
    );

    const gruposVisiveis = gruposCadastrados.filter(grupo =>
        grupo.ativo === true ||
        gruposAtuais.has(Number(grupo.id))
    );

    if (!gruposVisiveis.length) {
        const aviso = document.createElement("p");

        aviso.textContent = "Não há grupos ativos cadastrados.";

        listaGruposDisponiveis.appendChild(aviso);

        return;
    }

    gruposVisiveis.forEach(grupo => {
        const grupoId = Number(grupo.id);
        const jaAssociado = gruposAtuais.has(grupoId);

        const label = document.createElement("label");
        const checkbox = document.createElement("input");
        const texto = document.createElement("span");

        label.className = "opcao-grupo";

        checkbox.type = "checkbox";
        checkbox.value = String(grupoId);
        checkbox.checked = jaAssociado;

        checkbox.disabled =
            grupo.ativo !== true && !jaAssociado;

        checkbox.setAttribute(
            "aria-label",
            `Associar ao grupo ${grupo.nome}`
        );

        texto.textContent = grupo.ativo === true
            ? grupo.nome
            : `${grupo.nome} (inativo)`;

        label.appendChild(checkbox);
        label.appendChild(texto);

        listaGruposDisponiveis.appendChild(label);
    });
}

function renderizarHistoricoGrupos() {
    historicoGrupos.replaceChildren();

    if (!alocacoesFuncionario.length) {
        const item = document.createElement("li");

        item.textContent =
            "Nenhuma associação registrada até o momento.";

        historicoGrupos.appendChild(item);

        return;
    }

    alocacoesFuncionario.forEach(alocacao => {
        const item = document.createElement("li");

        const titulo = document.createElement("strong");
        const detalhe = document.createElement("span");

        titulo.textContent = alocacao.atual
            ? `${alocacao.grupoNome} — atual`
            : alocacao.grupoNome;

        detalhe.textContent = alocacao.dataFim
            ? `Início: ${formatarData(alocacao.dataInicio)} | Encerramento: ${formatarData(alocacao.dataFim)}`
            : `Início: ${formatarData(alocacao.dataInicio)} | Associação vigente`;

        item.appendChild(titulo);
        item.appendChild(detalhe);

        historicoGrupos.appendChild(item);
    });
}

async function abrirDialogGrupos(
    funcionarioId,
    funcionarioNome,
    funcionarioAtivo
) {
    funcionarioEmEdicaoGrupos = {
        id: Number(funcionarioId),
        nome: funcionarioNome,
        ativo: funcionarioAtivo === true ||
            funcionarioAtivo === "true"
    };

    gruposCadastrados = [];
    alocacoesFuncionario = [];

    botaoSalvarGrupos.disabled =
        funcionarioEmEdicaoGrupos.ativo !== true;

    nomeFuncionarioGrupos.textContent =
        funcionarioEmEdicaoGrupos.ativo
            ? funcionarioNome
            : `${funcionarioNome} — funcionário inativo`;

    ajudaDialogGrupos.textContent =
        funcionarioEmEdicaoGrupos.ativo
            ? "Selecione um ou mais grupos. Ao retirar um grupo, o vínculo anterior será encerrado e permanecerá no histórico."
            : "Funcionário inativo. As associações anteriores permanecem disponíveis para consulta no histórico.";

    listaGruposDisponiveis.textContent =
        "Carregando grupos...";

    historicoGrupos.replaceChildren();

    const carregando = document.createElement("li");

    carregando.textContent = "Carregando histórico...";

    historicoGrupos.appendChild(carregando);

    exibirMensagem(mensagemGrupos, "");

    if (typeof dialogGruposFuncionario.showModal === "function") {
        dialogGruposFuncionario.showModal();
    } else {
        dialogGruposFuncionario.setAttribute("open", "");
    }

    try {
        const [
            respostaGrupos,
            respostaHistorico
        ] = await Promise.all([
            requisitarApi("/grupos"),

            requisitarApi(
                `/funcionarios/${encodeURIComponent(funcionarioId)}/alocacoes-grupos`
            )
        ]);

        if (!respostaGrupos || !respostaHistorico) {
            return;
        }

        if (!respostaGrupos.ok) {
            throw new Error(
                await obterMensagemErro(respostaGrupos)
            );
        }

        if (!respostaHistorico.ok) {
            throw new Error(
                await obterMensagemErro(respostaHistorico)
            );
        }

        gruposCadastrados = await respostaGrupos.json();

        alocacoesFuncionario = await respostaHistorico.json();

        renderizarOpcoesGrupos();
        renderizarHistoricoGrupos();

    } catch (erro) {
        console.error(erro);

        listaGruposDisponiveis.textContent =
            "Não foi possível carregar os grupos disponíveis.";

        exibirMensagem(
            mensagemGrupos,
            erro.message ||
                "Não foi possível carregar os dados de grupos."
        );
    }
}

async function salvarGruposFuncionario() {
    if (!funcionarioEmEdicaoGrupos) {
        return;
    }

    if (funcionarioEmEdicaoGrupos.ativo !== true) {
        return;
    }

    const grupoIds = Array.from(
        listaGruposDisponiveis.querySelectorAll(
            'input[type="checkbox"]:checked'
        )
    ).map(checkbox => Number(checkbox.value));

    const gruposAtuais = alocacoesFuncionario.filter(
        alocacao => alocacao.atual === true
    );

    if (!grupoIds.length && gruposAtuais.length) {

        const confirmado = window.confirm(
            `Deseja remover todos os grupos atuais de ${funcionarioEmEdicaoGrupos.nome}? O histórico será preservado.`
        );

        if (!confirmado) {
            return;
        }
    }

    botaoSalvarGrupos.disabled = true;

    exibirMensagem(mensagemGrupos, "");

    try {
        const resposta = await requisitarApi(
            `/funcionarios/${encodeURIComponent(funcionarioEmEdicaoGrupos.id)}/grupos`,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    grupoIds: grupoIds
                })
            }
        );

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            exibirMensagem(
                mensagemGrupos,
                await obterMensagemErro(resposta)
            );

            return;
        }

        alocacoesFuncionario = await resposta.json();

        renderizarOpcoesGrupos();
        renderizarHistoricoGrupos();

        exibirMensagem(
            mensagemGrupos,
            "Grupos do funcionário atualizados com sucesso.",
            "sucesso"
        );

        await carregarFuncionarios();

    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemGrupos,
            "Não foi possível atualizar os grupos. Verifique a conexão com o servidor."
        );

    } finally {
        botaoSalvarGrupos.disabled =
            funcionarioEmEdicaoGrupos?.ativo !== true;
    }
}

function fecharDialogGrupos() {
    if (dialogGruposFuncionario.open) {
        dialogGruposFuncionario.close();
    } else {
        dialogGruposFuncionario.removeAttribute("open");
    }

    funcionarioEmEdicaoGrupos = null;
}

function lerEndereco(sufixo) {
    const obterValor = campo =>
        document.getElementById(`${campo}${sufixo}`).value.trim();

    return {
        cep: obterValor("cep"),
        logradouro: obterValor("logradouro"),
        numero: obterValor("numero"),
        complemento: obterValor("complemento"),
        bairro: obterValor("bairro"),
        cidade: obterValor("cidade"),
        uf: obterValor("uf").toUpperCase()
    };
}

function preencherEndereco(sufixo, endereco = {}) {
    const campos = [
        "cep",
        "logradouro",
        "numero",
        "complemento",
        "bairro",
        "cidade",
        "uf"
    ];

    campos.forEach(campo => {
        const elemento = document.getElementById(`${campo}${sufixo}`);

        elemento.value = endereco[campo] || "";
    });
}

function configurarMascaraCep(campo) {
    campo.addEventListener("input", () => {
        const digitos = campo.value
            .replace(/\D/g, "")
            .slice(0, 8);

        campo.value = digitos.length > 5
            ? `${digitos.slice(0, 5)}-${digitos.slice(5)}`
            : digitos;
    });
}

function configurarCampoUf(campo) {
    campo.addEventListener("input", () => {
        campo.value = campo.value
            .replace(/[^a-zA-Z]/g, "")
            .slice(0, 2)
            .toUpperCase();
    });
}

async function abrirDialogEndereco(funcionarioId, funcionarioNome) {
    funcionarioEmEdicaoEndereco = Number(funcionarioId);

    nomeFuncionarioEndereco.textContent = funcionarioNome;
    preencherEndereco("Endereco");
    exibirMensagem(mensagemEndereco, "");
    botaoSalvarEndereco.disabled = true;

    if (typeof dialogEnderecoFuncionario.showModal === "function") {
        dialogEnderecoFuncionario.showModal();
    } else {
        dialogEnderecoFuncionario.setAttribute("open", "");
    }

    try {
        const resposta = await requisitarApi(
            `/funcionarios/${encodeURIComponent(funcionarioId)}/endereco`
        );

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            throw new Error(await obterMensagemErro(resposta));
        }

        const endereco = await resposta.json();

        preencherEndereco("Endereco", endereco);
        botaoSalvarEndereco.disabled = false;

    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemEndereco,
            erro.message || "Não foi possível carregar o endereço do funcionário."
        );
    }
}

async function salvarEnderecoFuncionario() {
    if (!funcionarioEmEdicaoEndereco) {
        return;
    }

    const dados = lerEndereco("Endereco");

    botaoSalvarEndereco.disabled = true;
    exibirMensagem(mensagemEndereco, "");

    try {
        const resposta = await requisitarApi(
            `/funcionarios/${encodeURIComponent(funcionarioEmEdicaoEndereco)}/endereco`,
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
            exibirMensagem(
                mensagemEndereco,
                await obterMensagemErro(resposta)
            );

            return;
        }

        const enderecoSalvo = await resposta.json();

        preencherEndereco("Endereco", enderecoSalvo);

        exibirMensagem(
            mensagemEndereco,
            "Endereço do funcionário atualizado com sucesso.",
            "sucesso"
        );

    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemEndereco,
            "Não foi possível salvar o endereço. Verifique a conexão com o servidor."
        );

    } finally {
        botaoSalvarEndereco.disabled =
            funcionarioEmEdicaoEndereco === null;
    }
}

function fecharDialogEndereco() {
    if (dialogEnderecoFuncionario.open) {
        dialogEnderecoFuncionario.close();
    } else {
        dialogEnderecoFuncionario.removeAttribute("open");
    }

    funcionarioEmEdicaoEndereco = null;
}

async function inativarFuncionario(id, nome, botao) {
    const confirmado = window.confirm(
        `Deseja realmente inativar o funcionário ${nome}? O registro e o histórico serão mantidos, mas ele não poderá mais entrar no sistema.`
    );

    if (!confirmado) {
        return;
    }

    botao.disabled = true;

    exibirMensagem(mensagemLista, "");

    try {
        const resposta = await requisitarApi(
            `/funcionarios/${encodeURIComponent(id)}/inativar`,
            {
                method: "PUT"
            }
        );

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            exibirMensagem(
                mensagemLista,
                await obterMensagemErro(resposta)
            );

            return;
        }

        await carregarFuncionarios();

        exibirMensagem(
            mensagemLista,
            `Funcionário ${nome} inativado com sucesso.`,
            "sucesso"
        );

    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemLista,
            "Não foi possível inativar o funcionário. Verifique a conexão com o backend."
        );

    } finally {
        botao.disabled = false;
    }
}

formulario.addEventListener("submit", async evento => {
    evento.preventDefault();

    exibirMensagem(mensagemCadastro, "");

    const acessoSistema = campoAcesso.value === "true";

    const senha = campoSenha.value;
    const confirmarSenha = campoConfirmarSenha.value;

    if (acessoSistema && (!senha.trim() || !confirmarSenha.trim())) {
        exibirMensagem(
            mensagemCadastro,
            "Preencha os dois campos de senha."
        );

        return;
    }

    if (acessoSistema && senha.length < 8) {
        exibirMensagem(
            mensagemCadastro,
            "A senha deve ter pelo menos 8 caracteres."
        );

        campoSenha.focus();

        return;
    }

    if (acessoSistema && senha !== confirmarSenha) {
        exibirMensagem(
            mensagemCadastro,
            "As senhas não coincidem. Confira os dois campos."
        );

        campoConfirmarSenha.focus();

        return;
    }

    const dados = {
        nome: document.getElementById("nome").value.trim(),
        matricula: document.getElementById("matricula").value.trim(),
        cargo: document.getElementById("cargo").value.trim(),
        salario: Number(document.getElementById("salario").value),
        acessoSistema,
        senha: acessoSistema ? senha : null,
        endereco: lerEndereco("Cadastro")
    };

    if (!dados.nome || !dados.matricula || !dados.cargo) {
        exibirMensagem(
            mensagemCadastro,
            "Preencha o nome, a matrícula e o cargo."
        );

        return;
    }

    if (!Number.isFinite(dados.salario) || dados.salario < 0) {
        exibirMensagem(
            mensagemCadastro,
            "Informe um salário válido."
        );

        return;
    }

    const botaoCadastrar = formulario.querySelector(
        'button[type="submit"]'
    );

    botaoCadastrar.disabled = true;

    try {
        const resposta = await requisitarApi("/autenticacao/cadastro", {
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
            exibirMensagem(
                mensagemCadastro,
                await obterMensagemErro(resposta)
            );

            return;
        }

        exibirMensagem(
            mensagemCadastro,
            "Funcionário cadastrado com sucesso.",
            "sucesso"
        );

        formulario.reset();

        configurarCampoSenha();

        await carregarFuncionarios();

    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemCadastro,
            "Não foi possível conectar ao servidor."
        );

    } finally {
        botaoCadastrar.disabled = false;
    }
});

campoAcesso.addEventListener(
    "change",
    configurarCampoSenha
);

listaFuncionarios.addEventListener("click", evento => {
    const botao = evento.target.closest("button[data-acao]");

    if (!botao) {
        return;
    }

    if (botao.dataset.acao === "grupos") {
        abrirDialogGrupos(
            botao.dataset.id,
            botao.dataset.nome,
            botao.dataset.ativo
        );

        return;
    }

    if (botao.dataset.acao === "endereco") {
        abrirDialogEndereco(
            botao.dataset.id,
            botao.dataset.nome
        );

        return;
    }

    if (botao.dataset.acao === "inativar") {
        inativarFuncionario(
            botao.dataset.id,
            botao.dataset.nome,
            botao
        );
    }
});

document.getElementById("botaoFecharDialogGrupos").addEventListener(
    "click",
    fecharDialogGrupos
);

document.getElementById("botaoCancelarGrupos").addEventListener(
    "click",
    fecharDialogGrupos
);

botaoSalvarGrupos.addEventListener(
    "click",
    salvarGruposFuncionario
);

document.getElementById("botaoFecharDialogEndereco").addEventListener(
    "click",
    fecharDialogEndereco
);

document.getElementById("botaoCancelarEndereco").addEventListener(
    "click",
    fecharDialogEndereco
);

botaoSalvarEndereco.addEventListener(
    "click",
    salvarEnderecoFuncionario
);

dialogEnderecoFuncionario.addEventListener("click", evento => {
    if (evento.target === dialogEnderecoFuncionario) {
        fecharDialogEndereco();
    }
});

dialogEnderecoFuncionario.addEventListener("close", () => {
    funcionarioEmEdicaoEndereco = null;
});

dialogGruposFuncionario.addEventListener("click", evento => {
    if (evento.target === dialogGruposFuncionario) {
        fecharDialogGrupos();
    }
});

dialogGruposFuncionario.addEventListener("close", () => {
    funcionarioEmEdicaoGrupos = null;
});

document.getElementById("botaoAtualizar").addEventListener(
    "click",
    carregarFuncionarios
);

document.getElementById("botaoSomarSelecionados").addEventListener(
    "click",
    async () => {
        const selecionados = Array.from(
            document.querySelectorAll(".funcionarioSelecionado:checked")
        ).map(checkbox => checkbox.value);

        if (!selecionados.length) {
            resultadoSelecao.textContent =
                "Selecione pelo menos um funcionário ativo.";

            return;
        }

        const parametros = new URLSearchParams();

        selecionados.forEach(matricula => {
            parametros.append("matriculas", matricula);
        });

        try {
            const resposta = await requisitarApi(
                `/relatorios/funcionarios/soma-salarios/selecionados?${parametros.toString()}`
            );

            if (!resposta) {
                return;
            }

            if (!resposta.ok) {
                exibirMensagem(
                    mensagemLista,
                    await obterMensagemErro(resposta)
                );

                return;
            }

            const dados = await resposta.json();

            resultadoSelecao.textContent =
                `${dados.quantidadeFuncionarios} funcionário(s) selecionado(s). Soma dos salários: ${formatarMoeda(dados.somaSalarios)}.`;

        } catch (erro) {
            console.error(erro);

            resultadoSelecao.textContent =
                "Não foi possível calcular a soma dos salários.";
        }
    }
);

document.getElementById("botaoSair").addEventListener("click", () => {
    localStorage.removeItem("token");

    window.location.replace("login.html");
});

configurarOlhos();
configurarCampoSenha();

configurarMascaraCep(document.getElementById("cepCadastro"));
configurarMascaraCep(document.getElementById("cepEndereco"));
configurarCampoUf(document.getElementById("ufCadastro"));
configurarCampoUf(document.getElementById("ufEndereco"));

if (verificarPermissaoAdmin()) {
    carregarFuncionarios();
}