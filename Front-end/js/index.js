const token = localStorage.getItem("token");

function decodificarToken(token) {
    try {
        const partes = token.split(".");

        if (partes.length !== 3) {
            return null;
        }

        const payload = partes[1];

        const payloadBase64 = payload
            .replace(/-/g, "+")
            .replace(/_/g, "/");

        const payloadDecodificado = atob(payloadBase64);

        return JSON.parse(payloadDecodificado);
    } catch (erro) {
        return null;
    }
}

function verificarAutenticacao() {
    if (!token || token.trim() === "") {
        window.location.replace("login.html");
        return;
    }

    const dadosToken = decodificarToken(token);

    if (!dadosToken || !dadosToken.exp) {
        localStorage.removeItem("token");
        window.location.replace("login.html");
        return;
    }

    const agora = Math.floor(Date.now() / 1000);

    if (dadosToken.exp <= agora) {
        localStorage.removeItem("token");
        window.location.replace("login.html");
    }
}

verificarAutenticacao();