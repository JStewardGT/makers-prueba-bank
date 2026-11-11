package com.makers.loansystem.application.dto;

import com.makers.loansystem.domain.model.Loan;
import com.makers.loansystem.domain.model.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Detalle del préstamo")
public record LoanResponse(
    Long id,
    BigDecimal amount,
    Integer termMonths,
    LoanStatus status,
    Long userId,
    String userEmail,
    String rejectionReason,
    Instant createdAt,
    Instant updatedAt
) implements Serializable {
    public static LoanResponse fromDomain(Loan loan) {
        return new LoanResponse(
            loan.getId(),
            loan.getAmount(),
            loan.getTermMonths(),
            loan.getStatus(),
            loan.getUserId(),
            loan.getUserEmail(),
            loan.getRejectionReason(),
            loan.getCreatedAt(),
            loan.getUpdatedAt()
        );
    }
}
