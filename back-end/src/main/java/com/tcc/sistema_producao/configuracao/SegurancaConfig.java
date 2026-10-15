
package com.tcc.sistema_producao.configuracao;

import com.tcc.sistema_producao.servico.FuncionarioDetalhesServico;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SegurancaConfig {

    private final JwtAutenticacaoFiltro jwtAutenticacaoFiltro;

    public SegurancaConfig(JwtAutenticacaoFiltro jwtAutenticacaoFiltro) {
        this.jwtAutenticacaoFiltro = jwtAutenticacaoFiltro;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            FuncionarioDetalhesServico funcionarioDetalhesServico,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(funcionarioDetalhesServico);

        provider.setPasswordEncoder(passwordEncoder);

        return provider::authenticate;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();

        configuracao.setAllowedOrigins(List.of(
                "http://127.0.0.1:5500",
                "http://localhost:5500"
        ));

        configuracao.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        configuracao.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        UrlBasedCorsConfigurationSource fonte =
                new UrlBasedCorsConfigurationSource();

        fonte.registerCorsConfiguration("/**", configuracao);

        return fonte;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        ))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/autenticacao/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/autenticacao/cadastro"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/grupos/**",
                                "/produtos/**",
                                "/metas/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/grupos",
                                "/produtos",
                                "/metas"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/grupos/**",
                                "/produtos/**",
                                "/metas/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/grupos/**",
                                "/produtos/**",
                                "/metas/**"
                        ).hasRole("ADMIN")


                        .requestMatchers(
                                HttpMethod.GET,
                                "/relatorios/funcionarios",
                                "/relatorios/funcionarios/**"
                        ).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAutenticacaoFiltro,
                        org.springframework.security.web.authentication
                                .UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
