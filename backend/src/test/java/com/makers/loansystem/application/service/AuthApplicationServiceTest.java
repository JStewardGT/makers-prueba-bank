package com.makers.loansystem.application.service;

import com.makers.loansystem.application.dto.AuthResponse;
import com.makers.loansystem.application.dto.LoginRequest;
import com.makers.loansystem.application.dto.RegisterRequest;
import com.makers.loansystem.application.dto.UserResponse;
import com.makers.loansystem.domain.exception.BusinessRuleValidationException;
import com.makers.loansystem.domain.exception.EmailAlreadyExistsException;
import com.makers.loansystem.domain.model.Role;
import com.makers.loansystem.domain.model.User;
import com.makers.loansystem.domain.port.UserRepositoryPort;
import com.makers.loansystem.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - AuthApplicationService")
class AuthApplicationServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthApplicationService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "usuario@test.com", "encoded-pass", "Usuario Test", Role.ROLE_USER, Instant.now());
    }

    @Test
    @DisplayName("login: Debe retornar token JWT si credenciales son correctas")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("usuario@test.com", "123");
        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("123", "encoded-pass")).thenReturn(true);
        when(jwtTokenProvider.generateToken(sampleUser)).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.token());
        assertEquals("usuario@test.com", response.email());
        assertEquals(Role.ROLE_USER, response.role());
    }

    @Test
    @DisplayName("login: Debe lanzar excepción si contraseña no coincide")
    void shouldThrowExceptionWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("usuario@test.com", "wrong");
        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrong", "encoded-pass")).thenReturn(false);

        assertThrows(BusinessRuleValidationException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("login: Debe lanzar excepción si usuario no existe")
    void shouldThrowExceptionWhenUserNotFound() {
        LoginRequest request = new LoginRequest("noexiste@test.com", "123");
        when(userRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(BusinessRuleValidationException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("register: Debe registrar usuario nuevo con contraseña cifrada")
    void shouldRegisterNewUser() {
        RegisterRequest request = new RegisterRequest("Nuevo Usuario", "nuevo@test.com", "123");
        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("encoded-new-pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("nuevo@test.com", response.email());
        assertEquals(Role.ROLE_USER, response.role());
        verify(passwordEncoder).encode("123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: Debe lanzar EmailAlreadyExistsException si correo ya existe")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Duplicado", "usuario@test.com", "123");
        when(userRepository.existsByEmail("usuario@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
