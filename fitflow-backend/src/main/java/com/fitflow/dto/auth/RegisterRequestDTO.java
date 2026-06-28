package com.fitflow.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

// Validação na borda do sistema (DTO de entrada) — a entidade User não valida,
// pois ela representa o estado já consistente no banco.
@Data
public class RegisterRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    // BCrypt trunca senhas > 72 bytes — limitamos no input para evitar comportamento silencioso
    @NotBlank
    @Size(min = 6, max = 72)
    private String password;
}
