package com.makers.loansystem.domain.exception;

public class UnauthorizedLoanAccessException extends DomainException {
    public UnauthorizedLoanAccessException(String message) {
        super(message);
    }

    public UnauthorizedLoanAccessException(Long loanId, Long userId) {
        super(String.format("El usuario con ID %d no tiene autorización para acceder o modificar el préstamo con ID %d.", userId, loanId));
    }
}
