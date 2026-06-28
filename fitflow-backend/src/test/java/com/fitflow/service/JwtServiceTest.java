package com.fitflow.service;

import com.fitflow.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

// Testes unitários sem contexto Spring — JwtService não tem dependências externas,
// então podemos instanciá-lo diretamente e injetar propriedades via ReflectionTestUtils.
class JwtServiceTest {

    private JwtService jwtService;

    // Segredo de teste: 32 bytes (256 bits), suficiente para HS256.
    // Base64 de 32 bytes zero — fraco propositalmente para testes (nunca em produção).
    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString(new byte[32]);

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("$2a$10$hashedPassword")
                .build();
    }

    @Test
    void generateToken_shouldReturnNonNull() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotNull().isNotEmpty();
        // JWT tem 3 partes separadas por ponto
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_shouldReturnEmail() {
        String token = jwtService.generateToken(testUser);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void validateToken_shouldReturnTrueForValid() {
        String token = jwtService.generateToken(testUser);

        boolean isValid = jwtService.validateToken(token, testUser);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalseForExpired() throws InterruptedException {
        // Serviço configurado para expiração de 1ms — qualquer token gerado expira imediatamente
        JwtService shortLivedService = new JwtService();
        ReflectionTestUtils.setField(shortLivedService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(shortLivedService, "expiration", 1L);

        String token = shortLivedService.generateToken(testUser);
        Thread.sleep(10); // garante que o token expirou

        assertThat(shortLivedService.validateToken(token, testUser)).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForWrongUser() {
        String token = jwtService.generateToken(testUser);
        User wrongUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .password("$2a$10$hashedPassword")
                .build();

        assertThat(jwtService.validateToken(token, wrongUser)).isFalse();
    }
}
