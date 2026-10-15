const formularioLogin = document.getElementById("formularioLogin");
const mensagem = document.getElementById("mensagem");

formularioLogin.addEventListener("submit", async function (evento) {
    evento.preventDefault();

    const matricula = document.getElementById("matricula").value;
    const senha = document.getElementById("senha").value;

    mensagem.textContent = "";

    try {
        const resposta = await fetch(
            "http://localhost:8080/autenticacao/login",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    matricula: matricula,
                    senha: senha
                })
            }
        );

        if (!resposta.ok) {
            mensagem.textContent = "Matrícula ou senha inválida.";
            return;
        }

        const dados = await resposta.json();

        localStorage.setItem("token", dados.token);

        mensagem.textContent = "Login realizado com sucesso.";

        setTimeout(function () {
            window.location.href = "index.html";
        }, 500);

    } catch (erro) {
        mensagem.textContent = "Não foi possível conectar ao servidor.";
        console.error(erro);
    }
});