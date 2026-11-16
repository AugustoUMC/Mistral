const enderecoServidor = "http://localhost:8080";
const token = localStorage.getItem("token");

const secaoRegistroJornada = document.getElementById("secaoRegistroJornada");
const avisoSomenteConsulta = document.getElementById("avisoSomenteConsulta");
const formularioJornada = document.getElementById("formularioJornada");
const formularioConsultaJornada = document.getElementById("formularioConsultaJornada");

const funcionarioJornada = document.getElementById("funcionarioJornada");
const dataJornada = document.getElementById("dataJornada");
const grupoJornada = document.getElementById("grupoJornada");
const horarioInicio = document.getElementById("horarioInicio");
const horarioFim = document.getElementById("horarioFim");
const intervaloMinutos = document.getElementById("intervaloMinutos");

const funcionarioConsulta = document.getElementById("funcionarioConsulta");
const grupoConsulta = document.getElementById("grupoConsulta");
const dataInicialConsulta = document.getElementById("dataInicialConsulta");
const dataFinalConsulta = document.getElementById("dataFinalConsulta");

const listaJornadas = document.getElementById("listaJornadas");
const mensagemCadastroJornada = document.getElementById("mensagemCadastroJornada");
const mensagemConsultaJornada = document.getElementById("mensagemConsultaJornada");
const ajudaGruposData = document.getElementById("ajudaGruposData");

const botaoSalvarJornada = document.getElementById("botaoSalvarJornada");
const botaoCancelarEdicao = document.getElementById("botaoCancelarEdicao");

let funcionarios = [];
let grupos = [];
let registros = [];
let registroEmEdicaoId = null;
let numeroRequisicaoAlocacoes = 0;
let usuarioEhAdmin = false;


function obterHojeLocal() {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = String(hoje.getMonth() + 1).padStart(2, "0");
    const dia = String(hoje.getDate()).padStart(2, "0");

    return `${ano}-${mes}-${dia}`;
}

function decodificarToken(jwt) {
    try {
        if (!jwt) {
            return null;
        }

        const partes = jwt.split(".");

        if (partes.length !== 3) {
            return null;
        }

        const payload = partes[1]
            .replace(/-/g, "+")
            .replace(/_/g, "/");

        const base64 = payload.padEnd(
            payload.length + ((4 - payload.length % 4) % 4),
            "="
        );

        return JSON.parse(atob(base64));

    } catch (erro) {
        console.error("Não foi possível decodificar o token.", erro);
        return null;
    }
}

function verificarAutenticacao() {
    if (!token || token.trim() === "") {
        window.location.replace("login.html");
        return false;
    }

    const dados = decodificarToken(token);

    if (!dados || !dados.exp || dados.exp <= Math.floor(Date.now() / 1000)) {
        localStorage.removeItem("token");
        window.location.replace("login.html");
        return false;
    }

    usuarioEhAdmin = String(dados.perfil || "").toUpperCase() === "ADMIN";

    secaoRegistroJornada.hidden = !usuarioEhAdmin;
    avisoSomenteConsulta.hidden = usuarioEhAdmin;

    return true;
}

function exibirMensagem(elemento, texto, tipo = "erro") {
    elemento.textContent = texto;
    elemento.className = texto ? `mensagem ${tipo}` : "mensagem";
}

async function requisitarApi(caminho, opcoes = {}) {
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
        return "Seu perfil não tem permissão para realizar esta operação.";
    }

    const texto = await resposta.text();

    if (texto) {
        try {
            const dados = JSON.parse(texto);

            if (typeof dados === "string") {
                return dados;
            }

            return dados.erro || dados.message || dados.detail || dados.title ||
                "Não foi possível concluir a operação.";

        } catch {
            return texto;
        }
    }

    if (resposta.status === 404) {
        return "O funcionário, grupo ou apontamento informado não foi encontrado.";
    }

    if (resposta.status === 409) {
        return "O horário informado conflita com outro apontamento do funcionário.";
    }

    return "Não foi possível concluir a operação.";
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

function formatarHora(hora) {
    return hora ? String(hora).slice(0, 5) : "—";
}

function formatarDuracao(minutos) {
    const total = Math.max(0, Number(minutos || 0));
    const horas = Math.floor(total / 60);
    const minutosRestantes = total % 60;

    return `${horas}h ${String(minutosRestantes).padStart(2, "0")}min`;
}

function criarCelula(valor) {
    const celula = document.createElement("td");

    celula.textContent = valor === null || valor === undefined
        ? ""
        : String(valor);

    return celula;
}

function preencherSelectFuncionarios(select, opcaoInicial) {
    select.replaceChildren(new Option(opcaoInicial.texto, opcaoInicial.valor));

    funcionarios.forEach(funcionario => {
        select.add(
            new Option(
                `${funcionario.nome} (${funcionario.matricula})`,
                funcionario.id
            )
        );
    });
}

async function carregarFuncionarios() {
    const resposta = await requisitarApi("/jornadas/opcoes/funcionarios");

    if (!resposta) {
        return;
    }

    if (!resposta.ok) {
        throw new Error(await obterMensagemErro(resposta));
    }

    funcionarios = await resposta.json();

    preencherSelectFuncionarios(funcionarioConsulta, {
        texto: "Todos os funcionários",
        valor: ""
    });

    if (usuarioEhAdmin) {
        preencherSelectFuncionarios(funcionarioJornada, {
            texto: "Selecione um funcionário",
            valor: ""
        });
    }
}

async function carregarGruposConsulta() {
    const resposta = await requisitarApi("/grupos");

    if (!resposta) {
        return;
    }

    if (!resposta.ok) {
        throw new Error(await obterMensagemErro(resposta));
    }

    grupos = await resposta.json();

    grupoConsulta.replaceChildren(
        new Option("Todos os grupos", "")
    );

    grupos.forEach(grupo => {
        const nome = grupo.ativo === false
            ? `${grupo.nome} (inativo)`
            : grupo.nome;

        grupoConsulta.add(new Option(nome, grupo.id));
    });
}

async function carregarGruposPermitidosParaData() {
    const numeroRequisicao = ++numeroRequisicaoAlocacoes;
    const funcionarioId = funcionarioJornada.value;
    const data = dataJornada.value;

    grupoJornada.replaceChildren(
        new Option("Carregando grupos...", "")
    );
    grupoJornada.disabled = true;

    if (!funcionarioId || !data) {
        grupoJornada.replaceChildren(
            new Option("Selecione o funcionário e a data", "")
        );

        ajudaGruposData.textContent =
            "Selecione o funcionário e a data para consultar os grupos associados naquele período.";

        return;
    }

    ajudaGruposData.textContent = "Consultando o histórico de alocações...";

    try {
        const resposta = await requisitarApi(
            `/funcionarios/${encodeURIComponent(funcionarioId)}/alocacoes-grupos`
        );

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            throw new Error(await obterMensagemErro(resposta));
        }

        const historico = await resposta.json();

        if (numeroRequisicao !== numeroRequisicaoAlocacoes) {
            return;
        }

        const alocacoesValidas = historico.filter(alocacao => {
            const iniciou = alocacao.dataInicio <= data;
            const aindaValia = !alocacao.dataFim || alocacao.dataFim >= data;

            return iniciou && aindaValia;
        });

        const gruposUnicos = new Map();

        alocacoesValidas.forEach(alocacao => {
            gruposUnicos.set(Number(alocacao.grupoId), {
                id: Number(alocacao.grupoId),
                nome: alocacao.grupoNome
            });
        });

        grupoJornada.replaceChildren(
            new Option("Selecione um grupo", "")
        );

        Array.from(gruposUnicos.values())
            .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR"))
            .forEach(grupo => {
                grupoJornada.add(new Option(grupo.nome, grupo.id));
            });

        grupoJornada.disabled = gruposUnicos.size === 0;

        if (!gruposUnicos.size) {
            grupoJornada.replaceChildren(
                new Option("Nenhum grupo associado nessa data", "")
            );

            ajudaGruposData.textContent =
                "Não foi encontrada uma associação válida entre esse funcionário e um grupo na data selecionada.";
        } else {
            ajudaGruposData.textContent =
                "São exibidos somente os grupos aos quais o funcionário estava associado na data selecionada. O servidor também validará a associação.";
        }

    } catch (erro) {
        console.error(erro);

        if (numeroRequisicao !== numeroRequisicaoAlocacoes) {
            return;
        }

        grupoJornada.replaceChildren(
            new Option("Não foi possível carregar os grupos", "")
        );

        grupoJornada.disabled = true;

        ajudaGruposData.textContent = erro.message ||
            "Não foi possível consultar as alocações do funcionário.";
    }
}

async function carregarApontamentos(parametros = new URLSearchParams()) {
    exibirMensagem(mensagemConsultaJornada, "");

    listaJornadas.innerHTML =
        '<tr><td colspan="10">Carregando apontamentos...</td></tr>';

    const sufixo = parametros.toString()
        ? `?${parametros.toString()}`
        : "";

    try {
        const resposta = await requisitarApi(`/jornadas${sufixo}`);

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {
            throw new Error(await obterMensagemErro(resposta));
        }

        registros = await resposta.json();

        renderizarApontamentos();

        exibirMensagem(
            mensagemConsultaJornada,
            `${registros.length} apontamento(s) encontrado(s).`,
            "info"
        );

    } catch (erro) {
        console.error(erro);

        listaJornadas.innerHTML =
            '<tr><td colspan="10">Não foi possível carregar os apontamentos.</td></tr>';

        exibirMensagem(
            mensagemConsultaJornada,
            erro.message || "Não foi possível consultar as jornadas."
        );
    }
}

function renderizarApontamentos() {
    listaJornadas.replaceChildren();

    if (!registros.length) {
        listaJornadas.innerHTML =
            '<tr><td colspan="10">Nenhum apontamento encontrado.</td></tr>';
    } else {
        registros.forEach(registro => {
            const linha = document.createElement("tr");

            const periodo = registro.dataFim &&
                registro.dataFim !== registro.dataJornada
                ? `${formatarData(registro.dataJornada)} → ${formatarData(registro.dataFim)}`
                : formatarData(registro.dataJornada);

            linha.appendChild(criarCelula(periodo));
            linha.appendChild(criarCelula(registro.funcionarioNome));
            linha.appendChild(criarCelula(registro.matricula));
            linha.appendChild(criarCelula(registro.grupoProducaoNome));
            linha.appendChild(criarCelula(formatarHora(registro.horarioInicio)));
            linha.appendChild(criarCelula(formatarHora(registro.horarioFim)));
            linha.appendChild(criarCelula(formatarDuracao(registro.minutosBrutos)));
            linha.appendChild(criarCelula(`${registro.intervaloMinutos} min`));
            linha.appendChild(criarCelula(formatarDuracao(registro.minutosLiquidos)));

            const celulaAcao = document.createElement("td");

            if (usuarioEhAdmin) {
                const botaoEditar = document.createElement("button");

                botaoEditar.type = "button";
                botaoEditar.className = "botao-editar";
                botaoEditar.textContent = "Editar";
                botaoEditar.dataset.acao = "editar";
                botaoEditar.dataset.id = registro.id;

                botaoEditar.setAttribute(
                    "aria-label",
                    `Editar apontamento ${registro.id}`
                );

                celulaAcao.appendChild(botaoEditar);
            } else {
                celulaAcao.textContent = "Consulta";
            }

            linha.appendChild(celulaAcao);
            listaJornadas.appendChild(linha);
        });
    }

    document.getElementById("totalApontamentos").textContent =
        String(registros.length);

    const totalMinutosLiquidos = registros.reduce(
        (soma, registro) => soma + Number(registro.minutosLiquidos || 0),
        0
    );

    document.getElementById("totalHorasLiquidas").textContent =
        formatarDuracao(totalMinutosLiquidos);

    const funcionariosDistintos = new Set(
        registros.map(registro => Number(registro.funcionarioId))
    );

    document.getElementById("totalFuncionariosJornada").textContent =
        String(funcionariosDistintos.size);
}

function limparFormularioJornada() {
    formularioJornada.reset();

    registroEmEdicaoId = null;
    funcionarioJornada.disabled = false;
    dataJornada.value = obterHojeLocal();
    intervaloMinutos.value = "0";

    grupoJornada.replaceChildren(
        new Option("Selecione o funcionário e a data", "")
    );

    grupoJornada.disabled = true;

    document.getElementById("tituloFormularioJornada").textContent =
        "Registrar jornada";

    botaoSalvarJornada.textContent = "Salvar apontamento";
    botaoCancelarEdicao.hidden = true;

    ajudaGruposData.textContent =
        "Selecione o funcionário e a data para consultar os grupos associados naquele período.";

    exibirMensagem(mensagemCadastroJornada, "");
}

async function editarApontamento(registro) {
    if (!usuarioEhAdmin) {
        return;
    }

    limparFormularioJornada();

    registroEmEdicaoId = Number(registro.id);
    funcionarioJornada.value = String(registro.funcionarioId);
    funcionarioJornada.disabled = true;
    dataJornada.value = registro.dataJornada;
    horarioInicio.value = formatarHora(registro.horarioInicio);
    horarioFim.value = formatarHora(registro.horarioFim);
    intervaloMinutos.value = String(registro.intervaloMinutos);

    await carregarGruposPermitidosParaData();

    grupoJornada.value = String(registro.grupoProducaoId);

    document.getElementById("tituloFormularioJornada").textContent =
        `Editar apontamento #${registro.id}`;

    botaoSalvarJornada.textContent = "Atualizar apontamento";
    botaoCancelarEdicao.hidden = false;

    exibirMensagem(
        mensagemCadastroJornada,
        "Edite os horários ou o grupo. O funcionário deste apontamento não pode ser substituído.",
        "info"
    );

    secaoRegistroJornada.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

async function salvarApontamento(evento) {
    evento.preventDefault();

    if (!usuarioEhAdmin) {
        return;
    }

    exibirMensagem(mensagemCadastroJornada, "");

    if (!formularioJornada.reportValidity()) {
        return;
    }

    if (!funcionarioJornada.value || !grupoJornada.value) {
        exibirMensagem(
            mensagemCadastroJornada,
            "Selecione um funcionário e um grupo associado na data do apontamento."
        );
        return;
    }

    if (horarioInicio.value === horarioFim.value) {
        exibirMensagem(
            mensagemCadastroJornada,
            "Os horários de início e término não podem ser iguais."
        );
        return;
    }

    const dados = {
        funcionarioId: Number(funcionarioJornada.value),
        grupoProducaoId: Number(grupoJornada.value),
        dataJornada: dataJornada.value,
        horarioInicio: horarioInicio.value,
        horarioFim: horarioFim.value,
        intervaloMinutos: Number(intervaloMinutos.value)
    };

    if (!Number.isInteger(dados.intervaloMinutos) || dados.intervaloMinutos < 0) {
        exibirMensagem(
            mensagemCadastroJornada,
            "Informe a duração dos intervalos em minutos inteiros e não negativos."
        );
        return;
    }

    botaoSalvarJornada.disabled = true;

    const atualizando = registroEmEdicaoId !== null;

    const caminho = atualizando
        ? `/jornadas/${encodeURIComponent(registroEmEdicaoId)}`
        : "/jornadas";

    try {
        const resposta = await requisitarApi(caminho, {
            method: atualizando ? "PUT" : "POST",
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
                mensagemCadastroJornada,
                await obterMensagemErro(resposta)
            );

            return;
        }

        await resposta.json();

        const mensagemSucesso = atualizando
            ? "Apontamento de jornada atualizado com sucesso."
            : "Apontamento de jornada registrado com sucesso.";

        limparFormularioJornada();

        exibirMensagem(
            mensagemCadastroJornada,
            mensagemSucesso,
            "sucesso"
        );

        await carregarApontamentos(lerFiltrosConsulta());

    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemCadastroJornada,
            erro.message || "Não foi possível salvar o apontamento."
        );

    } finally {
        botaoSalvarJornada.disabled = false;
    }
}

function lerFiltrosConsulta() {
    const parametros = new URLSearchParams();

    if (funcionarioConsulta.value) {
        parametros.set("funcionarioId", funcionarioConsulta.value);
    }

    if (grupoConsulta.value) {
        parametros.set("grupoId", grupoConsulta.value);
    }

    if (dataInicialConsulta.value) {
        parametros.set("dataInicial", dataInicialConsulta.value);
    }

    if (dataFinalConsulta.value) {
        parametros.set("dataFinal", dataFinalConsulta.value);
    }

    return parametros;
}

async function consultarApontamentos(evento) {
    if (evento) {
        evento.preventDefault();
    }

    if (
        dataInicialConsulta.value &&
        dataFinalConsulta.value &&
        dataInicialConsulta.value > dataFinalConsulta.value
    ) {
        exibirMensagem(
            mensagemConsultaJornada,
            "A data inicial não pode ser posterior à data final."
        );

        return;
    }

    await carregarApontamentos(lerFiltrosConsulta());
}

async function inicializarPagina() {
    try {
        await Promise.all([
            carregarFuncionarios(),
            carregarGruposConsulta()
        ]);

        if (usuarioEhAdmin) {
            funcionarioJornada.addEventListener(
                "change",
                carregarGruposPermitidosParaData
            );

            dataJornada.addEventListener(
                "change",
                carregarGruposPermitidosParaData
            );

            formularioJornada.addEventListener(
                "submit",
                salvarApontamento
            );

            botaoCancelarEdicao.addEventListener(
                "click",
                limparFormularioJornada
            );

            dataJornada.value = obterHojeLocal();
            intervaloMinutos.value = "0";
        }

        await carregarApontamentos();

    } catch (erro) {
        console.error(erro);

        exibirMensagem(
            mensagemConsultaJornada,
            erro.message || "Não foi possível carregar os dados iniciais."
        );
    }
}

formularioConsultaJornada.addEventListener(
    "submit",
    consultarApontamentos
);

document.getElementById("botaoLimparConsulta").addEventListener("click", async () => {
    formularioConsultaJornada.reset();
    await carregarApontamentos();
});

document.getElementById("botaoAtualizarJornadas").addEventListener(
    "click",
    () => consultarApontamentos()
);

listaJornadas.addEventListener("click", evento => {
    const botao = evento.target.closest(
        'button[data-acao="editar"][data-id]'
    );

    if (!botao) {
        return;
    }

    const registro = registros.find(
        item => Number(item.id) === Number(botao.dataset.id)
    );

    if (registro) {
        editarApontamento(registro);
    }
});

document.getElementById("botaoSair").addEventListener("click", () => {
    localStorage.removeItem("token");
    window.location.replace("login.html");
});

if (verificarAutenticacao()) {
    inicializarPagina();
}