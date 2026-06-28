package com.fitflow.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @NotBlank
    @Email
    private String email;

    // Sem @Size mínimo: não queremos revelar a política de senhas na resposta de 400
    @NotBlank
    private String password;
}
