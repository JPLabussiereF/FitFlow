package com.fitflow.controller;

import com.fitflow.dto.auth.AuthResponseDTO;
import com.fitflow.dto.auth.LoginRequestDTO;
import com.fitflow.dto.auth.RegisterRequestDTO;
import com.fitflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller thin: delega toda lógica ao AuthService.
// Validação de entrada (@Valid) é responsabilidade do DTO, não do service.
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 201 Created — recurso (usuário + token) foi criado com sucesso
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    // 200 OK — autenticação bem-sucedida, token retornado
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
