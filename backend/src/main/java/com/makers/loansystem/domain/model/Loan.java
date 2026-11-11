package com.makers.loansystem.domain.model;

import com.makers.loansystem.domain.exception.BusinessRuleValidationException;
import com.makers.loansystem.domain.exception.InvalidStatusTransitionException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Loan {
    public static final BigDecimal MIN_AMOUNT = new BigDecimal("100.00");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("100000000.00");
    public static final int MIN_TERM_MONTHS = 1;
    public static final int MAX_TERM_MONTHS = 72;

    private Long id;
    private BigDecimal amount;
    private Integer termMonths;
    private LoanStatus status;
    private Long userId;
    private String userEmail;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public Loan() {
    }

    public Loan(Long id, BigDecimal amount, Integer termMonths, LoanStatus status,
                Long userId, String userEmail, String rejectionReason,
                Instant createdAt, Instant updatedAt, Long version) {
        this.id = id;
        this.amount = amount;
        this.termMonths = termMonths;
        this.status = status != null ? status : LoanStatus.PENDING;
        this.userId = userId;
        this.userEmail = userEmail;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version;
    }

    public static Loan request(BigDecimal amount, Integer termMonths, Long userId, String userEmail) {
        validateAmount(amount);
        validateTerm(termMonths);
        Objects.requireNonNull(userId, "El ID de usuario es obligatorio");
        Objects.requireNonNull(userEmail, "El correo de usuario es obligatorio");

        return new Loan(null, amount, termMonths, LoanStatus.PENDING,
                userId, userEmail, null, Instant.now(), Instant.now(), null);
    }

    public void approve() {
        if (this.status != LoanStatus.PENDING) {
            throw new InvalidStatusTransitionException(this.status, LoanStatus.APPROVED);
        }
        this.status = LoanStatus.APPROVED;
        this.rejectionReason = null;
        this.updatedAt = Instant.now();
    }

    public void reject(String reason) {
        if (this.status != LoanStatus.PENDING) {
            throw new InvalidStatusTransitionException(this.status, LoanStatus.REJECTED);
        }
        this.status = LoanStatus.REJECTED;
        this.rejectionReason = (reason != null && !reason.isBlank()) ? reason.trim() : "Rechazado por política de crédito";
        this.updatedAt = Instant.now();
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessRuleValidationException("El monto del préstamo es obligatorio");
        }
        if (amount.compareTo(MIN_AMOUNT) < 0 || amount.compareTo(MAX_AMOUNT) > 0) {
            throw new BusinessRuleValidationException(String.format(
                    "El monto del préstamo debe estar entre %s y %s", MIN_AMOUNT, MAX_AMOUNT));
        }
    }

    private static void validateTerm(Integer termMonths) {
        if (termMonths == null) {
            throw new BusinessRuleValidationException("El plazo en meses es obligatorio");
        }
        if (termMonths < MIN_TERM_MONTHS || termMonths > MAX_TERM_MONTHS) {
            throw new BusinessRuleValidationException(String.format(
                    "El plazo debe estar comprendido entre %d y %d meses", MIN_TERM_MONTHS, MAX_TERM_MONTHS));
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
