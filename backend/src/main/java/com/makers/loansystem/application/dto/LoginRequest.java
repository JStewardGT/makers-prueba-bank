package com.makers.loansystem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales de inicio de sesión")
public record LoginRequest(
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Formato de correo electrónico no válido")
    @Schema(description = "Correo del usuario", example = "usuario@test.com")
    String email,

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Schema(description = "Contraseña", example = "123")
    String password
) {}
