package com.makers.loansystem.domain.model;

public enum LoanStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED;
    }
}
