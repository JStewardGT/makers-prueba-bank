package com.makers.loansystem.application.dto;

import com.makers.loansystem.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación con token JWT")
public record AuthResponse(
    String token,
    String tokenType,
    Long userId,
    String email,
    String fullName,
    Role role
) {
    public static AuthResponse of(String token, Long userId, String email, String fullName, Role role) {
        return new AuthResponse(token, "Bearer", userId, email, fullName, role);
    }
}
