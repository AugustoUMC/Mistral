const enderecoServidor = "http://localhost:8080";

const formularioFeriado = document.getElementById("formularioFeriado");
const ano = document.getElementById("ano");
const listaFeriados = document.getElementById("listaFeriados");
const mensagem = document.getElementById("mensagem");
const botaoSair = document.getElementById("botaoSair");

const token = localStorage.getItem("token");

function verificarToken() {

    if (!token || token.trim() === "") {
        window.location.replace("login.html");
        return false;
    }

    return true;
}

async function consultarFeriados(anoConsulta) {

    const resposta = await fetch(
        `${enderecoServidor}/feriados/${anoConsulta}`,
        {
            method: "GET",
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    if (resposta.status === 401 || resposta.status === 403) {
        localStorage.removeItem("token");
        window.location.replace("login.html");
        return null;
    }

    return resposta;
}

function mostrarMensagem(texto, sucesso = false) {

    mensagem.textContent = texto;
    mensagem.style.color = sucesso ? "#16a34a" : "#dc2626";
}

function formatarData(data) {

    const partes = data.split("-");

    if (partes.length !== 3) {
        return data;
    }

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
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

async function carregarFeriados(anoConsulta) {

    try {

        mostrarMensagem("Consultando feriados...");

        const resposta = await consultarFeriados(anoConsulta);

        if (!resposta) {
            return;
        }

        if (!resposta.ok) {

            mostrarMensagem(
                "Não foi possível consultar os feriados."
            );

            return;
        }

        const feriados = await resposta.json();

        if (!feriados || feriados.length === 0) {

            listaFeriados.innerHTML = `
                <tr>
                    <td colspan="4">
                        Nenhum feriado encontrado.
                    </td>
                </tr>
            `;

            mostrarMensagem(
                "Nenhum feriado encontrado.",
                true
            );

            return;
        }

        listaFeriados.innerHTML = "";

        feriados.forEach(function (feriado) {

            const linha = document.createElement("tr");

            linha.innerHTML = `
                <td>${escaparHtml(formatarData(feriado.date))}</td>
                <td>${escaparHtml(feriado.name)}</td>
                <td>${escaparHtml(feriado.type)}</td>
                <td>${escaparHtml(feriado.weekday)}</td>
            `;

            listaFeriados.appendChild(linha);
        });

        mostrarMensagem(
            `${feriados.length} feriado(s) encontrado(s).`,
            true
        );

    } catch (erro) {

        console.error(erro);

        mostrarMensagem(
            "Não foi possível conectar ao servidor."
        );
    }
}

formularioFeriado.addEventListener(
    "submit",
    async function (evento) {

        evento.preventDefault();

        const anoConsulta = Number(ano.value);

        if (
            !anoConsulta ||
            anoConsulta < 1900 ||
            anoConsulta > 2199
        ) {

            mostrarMensagem(
                "Digite um ano válido."
            );

            return;
        }

        await carregarFeriados(anoConsulta);
    }
);

botaoSair.addEventListener("click", function () {

    localStorage.removeItem("token");

    window.location.replace("login.html");
});

if (verificarToken()) {
    carregarFeriados(Number(ano.value));
}