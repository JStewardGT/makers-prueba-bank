package com.makers.loansystem.application.port.in;

import com.makers.loansystem.application.dto.LoanResponse;

public interface ApproveLoanUseCase {
    LoanResponse approveLoan(Long loanId);
}
