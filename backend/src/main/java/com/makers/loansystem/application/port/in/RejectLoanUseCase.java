package com.makers.loansystem.application.port.in;

import com.makers.loansystem.application.dto.LoanResponse;
import com.makers.loansystem.application.dto.RejectLoanRequest;

public interface RejectLoanUseCase {
    LoanResponse rejectLoan(Long loanId, RejectLoanRequest request);
}
