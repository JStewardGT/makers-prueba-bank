package com.makers.loansystem.domain.model;

import com.makers.loansystem.domain.exception.BusinessRuleValidationException;
import com.makers.loansystem.domain.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - Modelo de Dominio Loan")
class LoanTest {

    @Test
    @DisplayName("Debe crear una solicitud de préstamo válida con estado PENDING")
    void shouldCreateValidLoanRequest() {
        BigDecimal amount = new BigDecimal("15000.00");
        Integer termMonths = 24;
        Long userId = 1L;
        String email = "usuario@test.com";

        Loan loan = Loan.request(amount, termMonths, userId, email);

        assertNotNull(loan);
        assertEquals(amount, loan.getAmount());
        assertEquals(termMonths, loan.getTermMonths());
        assertEquals(LoanStatus.PENDING, loan.getStatus());
        assertEquals(userId, loan.getUserId());
        assertEquals(email, loan.getUserEmail());
        assertNull(loan.getRejectionReason());
        assertNotNull(loan.getCreatedAt());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el monto es menor al mínimo permitido ($100)")
    void shouldThrowExceptionWhenAmountIsBelowMinimum() {
        BigDecimal amount = new BigDecimal("50.00");
        Integer termMonths = 12;

        BusinessRuleValidationException ex = assertThrows(BusinessRuleValidationException.class, () ->
                Loan.request(amount, termMonths, 1L, "user@test.com"));

        assertTrue(ex.getMessage().contains("debe estar entre"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el monto excede el máximo permitido ($100,000,000)")
    void shouldThrowExceptionWhenAmountExceedsMaximum() {
        BigDecimal amount = new BigDecimal("100000000.01");
        Integer termMonths = 12;

        BusinessRuleValidationException ex = assertThrows(BusinessRuleValidationException.class, () ->
                Loan.request(amount, termMonths, 1L, "user@test.com"));

        assertTrue(ex.getMessage().contains("debe estar entre"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el plazo en meses está fuera del rango 1 a 72")
    void shouldThrowExceptionWhenTermMonthsIsInvalid() {
        assertThrows(BusinessRuleValidationException.class, () ->
                Loan.request(new BigDecimal("1000.00"), 0, 1L, "user@test.com"));

        assertThrows(BusinessRuleValidationException.class, () ->
                Loan.request(new BigDecimal("1000.00"), 73, 1L, "user@test.com"));
    }

    @Test
    @DisplayName("Debe aprobar exitosamente un préstamo en estado PENDING")
    void shouldApprovePendingLoan() {
        Loan loan = Loan.request(new BigDecimal("5000.00"), 12, 1L, "user@test.com");

        loan.approve();

        assertEquals(LoanStatus.APPROVED, loan.getStatus());
        assertNull(loan.getRejectionReason());
    }

    @Test
    @DisplayName("Debe rechazar exitosamente un préstamo en estado PENDING con motivo")
    void shouldRejectPendingLoanWithReason() {
        Loan loan = Loan.request(new BigDecimal("5000.00"), 12, 1L, "user@test.com");
        String reason = "Historial crediticio insuficiente";

        loan.reject(reason);

        assertEquals(LoanStatus.REJECTED, loan.getStatus());
        assertEquals(reason, loan.getRejectionReason());
    }

    @Test
    @DisplayName("Debe fallar al intentar aprobar un préstamo ya aprobado")
    void shouldThrowExceptionWhenApprovingAlreadyApprovedLoan() {
        Loan loan = Loan.request(new BigDecimal("5000.00"), 12, 1L, "user@test.com");
        loan.approve();

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class, loan::approve);
        assertTrue(ex.getMessage().contains("Transición de estado inválida"));
    }

    @Test
    @DisplayName("Debe fallar al intentar rechazar un préstamo ya aprobado")
    void shouldThrowExceptionWhenRejectingAlreadyApprovedLoan() {
        Loan loan = Loan.request(new BigDecimal("5000.00"), 12, 1L, "user@test.com");
        loan.approve();

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class, () ->
                loan.reject("Algún motivo"));
        assertTrue(ex.getMessage().contains("Transición de estado inválida"));
    }

    @Test
    @DisplayName("Debe fallar al intentar aprobar un préstamo ya rechazado")
    void shouldThrowExceptionWhenApprovingAlreadyRejectedLoan() {
        Loan loan = Loan.request(new BigDecimal("5000.00"), 12, 1L, "user@test.com");
        loan.reject("Rechazado");

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class, loan::approve);
        assertTrue(ex.getMessage().contains("Transición de estado inválida"));
    }
}
