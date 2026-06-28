package com.fitflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitflow.config.ApplicationConfig;
import com.fitflow.config.SecurityConfig;
import com.fitflow.dto.auth.AuthResponseDTO;
import com.fitflow.dto.auth.LoginRequestDTO;
import com.fitflow.dto.auth.RegisterRequestDTO;
import com.fitflow.exception.EmailAlreadyExistsException;
import com.fitflow.model.User;
import com.fitflow.service.AuthService;
import com.fitflow.service.JwtService;
import com.fitflow.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @Import carrega SecurityConfig (rotas públicas/protegidas) e ApplicationConfig (PasswordEncoder, AuthProvider).
// JwtAuthenticationFilter é @Component que estende Filter — carregado automaticamente pelo slice @WebMvcTest.
// JwtService e UserService são @MockBean: injetados no filtro e usados nos testes de proteção de rota (US3).
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, ApplicationConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Necessário para JwtAuthenticationFilter (injetado automaticamente pelo slice)
    @MockBean
    private JwtService jwtService;

    // Necessário para JwtAuthenticationFilter e ApplicationConfig
    @MockBean
    private UserService userService;

    // ─── US1: Cadastro ────────────────────────────────────────────────────────

    @Test
    void register_shouldReturn201ForValidRequest() throws Exception {
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt-token").userId(1L).name("Test User").email("test@example.com")
                .build();
        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void register_shouldReturn409ForDuplicateEmail() throws Exception {
        when(authService.register(any()))
                .thenThrow(new EmailAlreadyExistsException("test@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void register_shouldReturn400ForInvalidEmail() throws Exception {
        RegisterRequestDTO dto = validRegisterRequest();
        dto.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400ForShortPassword() throws Exception {
        RegisterRequestDTO dto = validRegisterRequest();
        dto.setPassword("12345"); // menos de 6 chars

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400ForEmptyName() throws Exception {
        RegisterRequestDTO dto = validRegisterRequest();
        dto.setName("");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ─── US2: Login ───────────────────────────────────────────────────────────

    @Test
    void login_shouldReturn200ForValidCredentials() throws Exception {
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token("jwt-token").userId(1L).name("Test User").email("test@example.com")
                .build();
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_shouldReturn401ForWrongPassword() throws Exception {
        when(authService.login(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn401ForUnknownEmail() throws Exception {
        when(authService.login(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        LoginRequestDTO dto = validLoginRequest();
        dto.setEmail("unknown@example.com");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ─── US3: Proteção de Rotas ───────────────────────────────────────────────

    @Test
    void protectedRoute_shouldReturn401WithoutToken() throws Exception {
        // jwtService.extractUsername() retorna null por padrão (mock) — filtro não autentica
        mockMvc.perform(get("/api/v1/exercises"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRoute_shouldNotReturn401WithValidToken() throws Exception {
        // Configura o filtro para aceitar o token e autenticar o usuário
        User mockUser = User.builder()
                .id(1L).email("test@example.com").password("$2a$10$hash").build();
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("test@example.com");
        when(userService.loadUserByUsername("test@example.com")).thenReturn(mockUser);
        when(jwtService.validateToken("valid.jwt.token", mockUser)).thenReturn(true);

        // 404 porque a rota não existe ainda — o importante é NÃO ser 401
        mockMvc.perform(get("/api/v1/exercises")
                        .header("Authorization", "Bearer valid.jwt.token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void protectedRoute_shouldReturn401WithTamperedToken() throws Exception {
        // Token adulterado: extractUsername lança exceção — filtro não autentica
        when(jwtService.extractUsername("tampered.token")).thenThrow(
                new io.jsonwebtoken.JwtException("Invalid token"));

        mockMvc.perform(get("/api/v1/exercises")
                        .header("Authorization", "Bearer tampered.token"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private RegisterRequestDTO validRegisterRequest() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        return dto;
    }

    private LoginRequestDTO validLoginRequest() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        return dto;
    }
}
