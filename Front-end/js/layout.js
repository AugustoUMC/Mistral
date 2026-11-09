
(function () {
    "use strict";

    const paginas = {
        "index.html": {
            titulo: "Início",
            descricao: "Acompanhamento das atividades de produção"
        },
        "funcionarios.html": {
            titulo: "Funcionários",
            descricao: "Cadastro e consulta de funcionários"
        },
        "gerenciamento.html": {
            titulo: "Grupos e Metas",
            descricao: "Grupos de produção, produtos e metas"
        },
        "producao.html": {
            titulo: "Produção",
            descricao: "Registro e consulta da produção"
        },
        "relatorios.html": {
            titulo: "Relatórios",
            descricao: "Resultados e indicadores de produção"
        },
        "feriados.html": {
            titulo: "Feriados",
            descricao: "Consulta de feriados por ano"
        }
    };

    const itensMenu = [
        { titulo: "Início", arquivo: "index.html", grupo: "Principal" },
        { titulo: "Funcionários", arquivo: "funcionarios.html", grupo: "Gestão", apenasAdmin: true },
        { titulo: "Grupos e Metas", arquivo: "gerenciamento.html", grupo: "Gestão" },
        { titulo: "Produção", arquivo: "producao.html", grupo: "Operação" },
        { titulo: "Relatórios", arquivo: "relatorios.html", grupo: "Consultas" },
        { titulo: "Feriados", arquivo: "feriados.html", grupo: "Consultas" }
    ];

    function obterArquivoAtual() {
        const segmentos = window.location.pathname.split("/");
        return (segmentos.pop() || "index.html").toLowerCase();
    }

    function obterDadosToken() {
        const token = localStorage.getItem("token");

        if (!token) {
            return null;
        }

        try {
            const partes = token.split(".");

            if (partes.length !== 3) {
                return null;
            }

            let payload = partes[1]
                .replace(/-/g, "+")
                .replace(/_/g, "/");

            payload += "=".repeat((4 - payload.length % 4) % 4);

            const binario = atob(payload);
            const bytes = Uint8Array.from(
                binario,
                caractere => caractere.charCodeAt(0)
            );

            const texto = new TextDecoder().decode(bytes);
            const dados = JSON.parse(texto);

            if (dados.exp && Number(dados.exp) * 1000 <= Date.now()) {
                return null;
            }

            return dados;

        } catch (erro) {
            console.error("Não foi possível ler o perfil do token.", erro);
            return null;
        }
    }

    function criarElemento(tag, classe, texto) {
        const elemento = document.createElement(tag);

        if (classe) {
            elemento.className = classe;
        }

        if (texto !== undefined) {
            elemento.textContent = texto;
        }

        return elemento;
    }

    function removerLinksForaDoEscopo(perfil) {
        document.querySelectorAll("a[href]").forEach(link => {
            let destino;

            try {
                destino = new URL(link.getAttribute("href"), window.location.href);
            } catch (erro) {
                return;
            }

            const arquivo = destino.pathname.split("/").pop().toLowerCase();

            if (arquivo === "jornada.html") {
                link.remove();
                return;
            }

            if (arquivo === "funcionarios.html" && perfil !== "ADMIN") {
                link.remove();
            }
        });
    }

    function criarMenu(arquivoAtual, perfil) {
        const menu = criarElemento("aside", "menu-lateral");
        menu.id = "menuPrincipal";
        menu.setAttribute("aria-label", "Menu principal");

        const marca = criarElemento("div", "menu-marca");

        marca.append(
            criarElemento("strong", "", "Mistral"),
            criarElemento("span", "", "CONTROLE DE PRODUÇÃO")
        );

        const navegacao = criarElemento("nav", "menu-navegacao");
        navegacao.setAttribute("aria-label", "Navegação principal");

        let grupoAtual = "";

        itensMenu.forEach(item => {
            if (item.apenasAdmin && perfil !== "ADMIN") {
                return;
            }

            if (grupoAtual !== item.grupo) {
                grupoAtual = item.grupo;

                navegacao.appendChild(
                    criarElemento("span", "menu-grupo-titulo", grupoAtual)
                );
            }

            const link = criarElemento("a", "menu-link", item.titulo);
            link.href = item.arquivo;

            if (arquivoAtual === item.arquivo) {
                link.classList.add("ativo");
                link.setAttribute("aria-current", "page");
            }

            navegacao.appendChild(link);
        });

        const rodape = criarElemento("div", "menu-rodape");

        rodape.textContent = perfil === "ADMIN"
            ? "Perfil administrador"
            : "Perfil usuário";

        menu.append(marca, navegacao, rodape);

        return menu;
    }

    function criarCabecalho(arquivoAtual, botaoSair, perfil) {
        const cabecalho = criarElemento("header", "cabecalho-global");
        const esquerda = criarElemento("div", "cabecalho-esquerda");
        const informacoes = criarElemento("div", "cabecalho-pagina-info");

        const dadosPagina = paginas[arquivoAtual];

        const botaoMenu = criarElemento(
            "button",
            "botao-menu-mobile",
            "☰"
        );

        botaoMenu.type = "button";
        botaoMenu.setAttribute("aria-label", "Abrir menu de navegação");
        botaoMenu.setAttribute("aria-controls", "menuPrincipal");
        botaoMenu.setAttribute("aria-expanded", "false");

        informacoes.append(
            criarElemento("h1", "", dadosPagina.titulo),
            criarElemento("p", "", dadosPagina.descricao)
        );

        esquerda.append(botaoMenu, informacoes);

        const acoes = criarElemento("div", "acoes-cabecalho");

        acoes.appendChild(
            criarElemento(
                "span",
                "identificacao-perfil",
                perfil === "ADMIN" ? "Administrador" : "Usuário"
            )
        );

        if (botaoSair) {
            botaoSair.type = "button";
            botaoSair.classList.add("botao-sair");

            // Mover o botão preserva o evento de logout já registrado.
            acoes.appendChild(botaoSair);
        }

        cabecalho.append(esquerda, acoes);

        return {
            cabecalho,
            botaoMenu
        };
    }

    function configurarMenuResponsivo(botaoMenu, fundo) {
        function fecharMenu() {
            document.body.classList.remove("menu-aberto");

            botaoMenu.setAttribute("aria-expanded", "false");
            botaoMenu.setAttribute(
                "aria-label",
                "Abrir menu de navegação"
            );
        }

        function alternarMenu() {
            const aberto = document.body.classList.toggle("menu-aberto");

            botaoMenu.setAttribute("aria-expanded", String(aberto));

            botaoMenu.setAttribute(
                "aria-label",
                aberto ? "Fechar menu de navegação" : "Abrir menu de navegação"
            );
        }

        botaoMenu.addEventListener("click", alternarMenu);
        fundo.addEventListener("click", fecharMenu);

        document.addEventListener("keydown", evento => {
            if (evento.key === "Escape") {
                fecharMenu();
            }
        });

        document.querySelectorAll(".menu-link").forEach(link => {
            link.addEventListener("click", fecharMenu);
        });

        window.addEventListener("resize", () => {
            if (window.innerWidth > 760) {
                fecharMenu();
            }
        });
    }

    function aplicarLayoutSistema() {
        const arquivoAtual = obterArquivoAtual();
        const dadosPagina = paginas[arquivoAtual];

        if (!dadosPagina) {
            return;
        }

        const cabecalhoOriginal = document.querySelector(
            "body > header.cabecalho"
        );

        const conteudo = document.querySelector(
            "body > main.conteudo"
        );

        if (
            !cabecalhoOriginal ||
            !conteudo ||
            document.querySelector(".app-layout")
        ) {
            console.error(
                "Não foi possível montar o layout: cabeçalho ou conteúdo não encontrado."
            );

            return;
        }

        const dadosToken = obterDadosToken();

        const perfil = String(
            dadosToken && dadosToken.perfil || ""
        ).toUpperCase();

        removerLinksForaDoEscopo(perfil);

        const botaoSair = cabecalhoOriginal.querySelector("#botaoSair");

        const estrutura = criarElemento("div", "app-layout");
        const principal = criarElemento("div", "app-principal");
        const fundo = criarElemento("button", "menu-fundo");

        fundo.type = "button";
        fundo.setAttribute("aria-label", "Fechar menu de navegação");

        const menu = criarMenu(arquivoAtual, perfil);

        const novoCabecalho = criarCabecalho(
            arquivoAtual,
            botaoSair,
            perfil
        );

        principal.append(
            novoCabecalho.cabecalho,
            conteudo
        );

        estrutura.append(
            menu,
            fundo,
            principal
        );

        cabecalhoOriginal.remove();

        document.body.insertBefore(
            estrutura,
            document.body.firstChild
        );

        document.body.classList.add("mistral-app");

        configurarMenuResponsivo(
            novoCabecalho.botaoMenu,
            fundo
        );
    }

    function aplicarLayoutPublico() {
        const arquivoAtual = obterArquivoAtual();

        const paginasPublicas = [
            "termos-uso.html",
            "politica-privacidade.html"
        ];

        if (!paginasPublicas.includes(arquivoAtual)) {
            return;
        }

        const cabecalhoOriginal = document.querySelector(
            "body > header.cabecalho"
        );

        if (!cabecalhoOriginal) {
            return;
        }

        const cabecalho = criarElemento(
            "header",
            "cabecalho-global cabecalho-publico"
        );

        const informacoes = criarElemento(
            "div",
            "cabecalho-pagina-info"
        );

        const acoes = criarElemento(
            "div",
            "acoes-cabecalho"
        );

        const titulo = arquivoAtual === "termos-uso.html"
            ? "Termos de Uso"
            : "Política de Privacidade";

        const linkLogin = criarElemento(
            "a",
            "botao-secundario",
            "Entrar"
        );

        linkLogin.href = "login.html";

        informacoes.append(
            criarElemento("h1", "", "Mistral"),
            criarElemento("p", "", titulo)
        );

        acoes.appendChild(linkLogin);

        cabecalho.append(
            informacoes,
            acoes
        );

        cabecalhoOriginal.replaceWith(cabecalho);

        document.body.classList.add("mistral-publico");
    }

    function iniciar() {
        const arquivoAtual = obterArquivoAtual();

        if (arquivoAtual === "login.html") {
            return;
        }

        aplicarLayoutPublico();
        aplicarLayoutSistema();
    }

    iniciar();
})();
