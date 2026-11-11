package com.makers.loansystem.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registro de nuevo usuario")
public record RegisterRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre completo", example = "Carlos Pérez")
    String fullName,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo electrónico inválido")
    @Schema(description = "Correo electrónico", example = "carlos@test.com")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 3, message = "La contraseña debe tener al menos 3 caracteres")
    @Schema(description = "Contraseña", example = "123")
    String password
) {}
