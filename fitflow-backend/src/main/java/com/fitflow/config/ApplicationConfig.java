package com.fitflow.config;

import com.fitflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Beans de infraestrutura de segurança separados do SecurityConfig para evitar
// dependências circulares — ApplicationConfig não depende de nenhum bean de segurança.
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserService userService;

    // BCrypt com custo 10 (padrão): ~100ms por hash em hardware moderno
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DaoAuthenticationProvider: carrega o usuário pelo email (via UserService)
    // e verifica a senha contra o hash BCrypt armazenado.
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Exposto como bean para que o AuthService possa chamar authenticate() diretamente.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
