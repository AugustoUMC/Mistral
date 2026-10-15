package com.tcc.sistema_producao.servico;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtServico {

    private final SecretKey chave;
    private final long expiracao;

    public JwtServico(
            @Value("${jwt.chave-secreta}") String chaveSecreta,
            @Value("${jwt.expiracao}") long expiracao) {

        this.chave = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(chaveSecreta)
        );

        this.expiracao = expiracao;
    }

    public String gerarToken(String matricula, String perfil) {
        Date agora = new Date();
        Date dataExpiracao = new Date(agora.getTime() + expiracao);

        return Jwts.builder()
                .subject(matricula)
                .claim("perfil", perfil)
                .issuedAt(agora)
                .expiration(dataExpiracao)
                .signWith(chave, Jwts.SIG.HS256)
                .compact();
    }

    public String extrairMatricula(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}