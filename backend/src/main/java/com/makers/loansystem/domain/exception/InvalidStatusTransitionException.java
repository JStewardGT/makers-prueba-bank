package com.makers.loansystem.domain.exception;

import com.makers.loansystem.domain.model.LoanStatus;

public class InvalidStatusTransitionException extends DomainException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(LoanStatus currentStatus, LoanStatus targetStatus) {
        super(String.format("Transición de estado inválida: no se puede cambiar de %s a %s. Solo préstamos PENDING pueden ser procesados.",
                currentStatus, targetStatus));
    }
}
