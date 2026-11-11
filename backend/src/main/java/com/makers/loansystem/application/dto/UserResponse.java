package com.makers.loansystem.application.dto;

import com.makers.loansystem.domain.model.Role;
import com.makers.loansystem.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Información del usuario")
public record UserResponse(
    Long id,
    String email,
    String fullName,
    Role role,
    Instant createdAt
) {
    public static UserResponse fromDomain(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getCreatedAt());
    }
}
