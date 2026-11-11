package com.makers.loansystem.application.dto;

import com.makers.loansystem.domain.model.Loan;
import com.makers.loansystem.domain.model.LoanStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record LoanStatusEvent(
    Long loanId,
    Long userId,
    String userEmail,
    BigDecimal amount,
    Integer termMonths,
    LoanStatus status,
    String reason,
    Instant timestamp
) {
    public static LoanStatusEvent fromLoan(Loan loan) {
        return new LoanStatusEvent(
            loan.getId(),
            loan.getUserId(),
            loan.getUserEmail(),
            loan.getAmount(),
            loan.getTermMonths(),
            loan.getStatus(),
            loan.getRejectionReason(),
            Instant.now()
        );
    }
}
