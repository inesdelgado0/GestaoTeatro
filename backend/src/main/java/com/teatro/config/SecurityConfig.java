package com.teatro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Objects;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/utilizadores/registo").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/eventos",
                                "/api/eventos/**",
                                "/api/sessoes",
                                "/api/sessoes/**",
                                "/api/salas",
                                "/api/salas/**",
                                "/api/zonas",
                                "/api/zonas/**",
                                "/api/lugares",
                                "/api/lugares/**",
                                "/api/tipos-bilhete",
                                "/api/tipos-bilhete/**",
                                "/api/relatorios",
                                "/api/relatorios/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/eventos",
                                "/api/eventos/**",
                                "/api/sessoes",
                                "/api/sessoes/**",
                                "/api/salas",
                                "/api/salas/**",
                                "/api/zonas",
                                "/api/zonas/**",
                                "/api/lugares",
                                "/api/lugares/**",
                                "/api/tipos-bilhete",
                                "/api/tipos-bilhete/**",
                                "/api/relatorios",
                                "/api/relatorios/**",
                                "/api/utilizadores/**"
                        ).hasAuthority("ROLE_ADMINISTRADOR")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return delegate.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null || encodedPassword.isBlank()) {
                    return false;
                }

                if (isBcryptHash(encodedPassword)) {
                    return delegate.matches(rawPassword, encodedPassword);
                }

                return Objects.equals(rawPassword.toString(), encodedPassword);
            }

            private boolean isBcryptHash(String value) {
                return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
            }
        };
    }
}
