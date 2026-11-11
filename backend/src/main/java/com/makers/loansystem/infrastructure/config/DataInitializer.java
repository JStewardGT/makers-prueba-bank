package com.makers.loansystem.infrastructure.config;

import com.makers.loansystem.domain.model.Loan;
import com.makers.loansystem.domain.model.LoanStatus;
import com.makers.loansystem.domain.model.Role;
import com.makers.loansystem.domain.model.User;
import com.makers.loansystem.domain.port.LoanRepositoryPort;
import com.makers.loansystem.domain.port.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepositoryPort userRepository;
    private final LoanRepositoryPort loanRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepositoryPort userRepository,
                           LoanRepositoryPort loanRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Iniciando verificación y siembra de datos iniciales...");

        // 1. Sembrar Administrador
        User admin = userRepository.findByEmail("admin@test.com").orElseGet(() -> {
            log.info("Creando usuario administrador semilla: admin@test.com");
            User newAdmin = User.create(
                    "admin@test.com",
                    passwordEncoder.encode("123"),
                    "Admin Admin",
                    Role.ROLE_ADMIN
            );
            return userRepository.save(newAdmin);
        });

        // 2. Sembrar Usuario Estándar
        User standardUser = userRepository.findByEmail("usuario@test.com").orElseGet(() -> {
            log.info("Creando usuario estándar semilla: usuario@test.com");
            User newUser = User.create(
                    "usuario@test.com",
                    passwordEncoder.encode("123"),
                    "Usuario Demo",
                    Role.ROLE_USER
            );
            return userRepository.save(newUser);
        });

        // 3. Sembrar préstamos iniciales si no existen
        if (loanRepository.findByUserId(standardUser.getId()).isEmpty()) {
            log.info("Sembrando solicitudes de préstamos de ejemplo para: {}", standardUser.getEmail());

            // Préstamo 1: $1000, 12 meses, Pendiente
            Loan loan1 = new Loan(
                    null,
                    new BigDecimal("1000.00"),
                    12,
                    LoanStatus.PENDING,
                    standardUser.getId(),
                    standardUser.getEmail(),
                    null,
                    Instant.now().minusSeconds(86400 * 2),
                    Instant.now().minusSeconds(86400 * 2),
                    null
            );
            loanRepository.save(loan1);

            // Préstamo 2: $2000, 24 meses, Aprobado
            Loan loan2 = new Loan(
                    null,
                    new BigDecimal("2000.00"),
                    24,
                    LoanStatus.APPROVED,
                    standardUser.getId(),
                    standardUser.getEmail(),
                    null,
                    Instant.now().minusSeconds(86400),
                    Instant.now().minusSeconds(3600),
                    null
            );
            loanRepository.save(loan2);

            // Préstamo 3: $30000, 36 meses, Pendiente
            Loan loan3 = new Loan(
                    null,
                    new BigDecimal("30000.00"),
                    36,
                    LoanStatus.PENDING,
                    standardUser.getId(),
                    standardUser.getEmail(),
                    null,
                    Instant.now(),
                    Instant.now(),
                    null
            );
            loanRepository.save(loan3);

            log.info("Datos iniciales sembrados exitosamente.");
        }
    }
}
