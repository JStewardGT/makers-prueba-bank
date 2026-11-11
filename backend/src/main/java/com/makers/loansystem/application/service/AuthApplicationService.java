package com.makers.loansystem.application.service;

import com.makers.loansystem.application.dto.AuthResponse;
import com.makers.loansystem.application.dto.LoginRequest;
import com.makers.loansystem.application.dto.RegisterRequest;
import com.makers.loansystem.application.dto.UserResponse;
import com.makers.loansystem.application.port.in.AuthUseCase;
import com.makers.loansystem.application.port.in.UserManagementUseCase;
import com.makers.loansystem.domain.exception.BusinessRuleValidationException;
import com.makers.loansystem.domain.exception.EmailAlreadyExistsException;
import com.makers.loansystem.domain.exception.ResourceNotFoundException;
import com.makers.loansystem.domain.model.Role;
import com.makers.loansystem.domain.model.User;
import com.makers.loansystem.domain.port.UserRepositoryPort;
import com.makers.loansystem.infrastructure.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthApplicationService implements AuthUseCase, UserManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthApplicationService.class);

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthApplicationService(UserRepositoryPort userRepository,
                                  PasswordEncoder passwordEncoder,
                                  JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de inicio de sesión para el usuario: {}", request.email());

        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BusinessRuleValidationException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Contraseña incorrecta para el usuario: {}", request.email());
            throw new BusinessRuleValidationException("Credenciales inválidas");
        }

        String token = jwtTokenProvider.generateToken(user);
        return AuthResponse.of(token, user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        log.info("Registrando nuevo usuario con correo: {}", normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(normalizedEmail, encodedPassword, request.fullName().trim(), Role.ROLE_USER);
        User saved = userRepository.save(user);

        return UserResponse.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::fromDomain)
                .orElseThrow(() -> ResourceNotFoundException.user(id));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw ResourceNotFoundException.user(id);
        }
        userRepository.deleteById(id);
    }
}
