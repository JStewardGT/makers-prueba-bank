package com.makers.loansystem.application.service;

import com.makers.loansystem.application.dto.CreateLoanRequest;
import com.makers.loansystem.application.dto.LoanResponse;
import com.makers.loansystem.application.dto.LoanStatusEvent;
import com.makers.loansystem.application.dto.RejectLoanRequest;
import com.makers.loansystem.application.port.in.ApproveLoanUseCase;
import com.makers.loansystem.application.port.in.CreateLoanUseCase;
import com.makers.loansystem.application.port.in.GetLoansUseCase;
import com.makers.loansystem.application.port.in.LoanEventStreamUseCase;
import com.makers.loansystem.application.port.in.RejectLoanUseCase;
import com.makers.loansystem.domain.exception.ResourceNotFoundException;
import com.makers.loansystem.domain.exception.UnauthorizedLoanAccessException;
import com.makers.loansystem.domain.model.Loan;
import com.makers.loansystem.domain.port.LoanRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoanApplicationService implements CreateLoanUseCase, ApproveLoanUseCase, RejectLoanUseCase, GetLoansUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationService.class);

    private final LoanRepositoryPort loanRepository;
    private final LoanEventStreamUseCase eventStream;
    private final ReactiveCreditScoreService creditScoreService;

    public LoanApplicationService(LoanRepositoryPort loanRepository,
                                  LoanEventStreamUseCase eventStream,
                                  ReactiveCreditScoreService creditScoreService) {
        this.loanRepository = loanRepository;
        this.eventStream = eventStream;
        this.creditScoreService = creditScoreService;
    }

    @Override
    @Transactional
    public LoanResponse createLoan(CreateLoanRequest request, Long userId, String userEmail) {
        log.info("Creando solicitud de préstamo para usuario {} por monto {}", userEmail, request.amount());

        // Creación del modelo de dominio validando invariantes
        Loan loan = Loan.request(request.amount(), request.termMonths(), userId, userEmail);
        Loan saved = loanRepository.save(loan);

        // Disparo reactivo en segundo plano para evaluación de riesgo y notificación SSE
        creditScoreService.evaluateCreditRisk(userId, request.amount(), request.termMonths())
                .subscribe(eval -> log.debug("Scoring reactivo obtenido para préstamo {}: {}", saved.getId(), eval));

        LoanStatusEvent event = LoanStatusEvent.fromLoan(saved);
        eventStream.publishEvent(event);

        return LoanResponse.fromDomain(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "loansCache", key = "#loanId")
    public LoanResponse approveLoan(Long loanId) {
        log.info("Aprobando préstamo ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> ResourceNotFoundException.loan(loanId));

        // Aplica regla de negocio pura de transición de estado
        loan.approve();
        Loan updated = loanRepository.save(loan);

        // Notificación reactiva SSE
        eventStream.publishEvent(LoanStatusEvent.fromLoan(updated));

        return LoanResponse.fromDomain(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "loansCache", key = "#loanId")
    public LoanResponse rejectLoan(Long loanId, RejectLoanRequest request) {
        String reason = (request != null) ? request.reason() : null;
        log.info("Rechazando préstamo ID: {} con motivo: {}", loanId, reason);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> ResourceNotFoundException.loan(loanId));

        // Aplica regla de negocio pura de transición de estado
        loan.reject(reason);
        Loan updated = loanRepository.save(loan);

        // Notificación reactiva SSE
        eventStream.publishEvent(LoanStatusEvent.fromLoan(updated));

        return LoanResponse.fromDomain(updated);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "loansCache", key = "#id")
    public LoanResponse getLoanById(Long id, Long currentUserId, boolean isAdmin) {
        log.info("Consultando préstamo ID: {} (consultado por usuario ID: {}, isAdmin: {})", id, currentUserId, isAdmin);

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.loan(id));

        if (!isAdmin && !loan.getUserId().equals(currentUserId)) {
            throw new UnauthorizedLoanAccessException(id, currentUserId);
        }

        return LoanResponse.fromDomain(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getMyLoans(Long currentUserId) {
        log.info("Consultando préstamos del usuario ID: {}", currentUserId);
        return loanRepository.findByUserId(currentUserId).stream()
                .map(LoanResponse::fromDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getAllLoans() {
        log.info("Consultando todos los préstamos registrados (Admin)");
        return loanRepository.findAll().stream()
                .map(LoanResponse::fromDomain)
                .toList();
    }
}
