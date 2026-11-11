package com.makers.loansystem.application.port.in;

import com.makers.loansystem.application.dto.AuthResponse;
import com.makers.loansystem.application.dto.LoginRequest;
import com.makers.loansystem.application.dto.RegisterRequest;
import com.makers.loansystem.application.dto.UserResponse;

public interface AuthUseCase {
    AuthResponse login(LoginRequest request);
    UserResponse register(RegisterRequest request);
}
