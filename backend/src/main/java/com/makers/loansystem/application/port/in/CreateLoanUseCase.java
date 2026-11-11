package com.makers.loansystem.application.port.in;

import com.makers.loansystem.application.dto.CreateLoanRequest;
import com.makers.loansystem.application.dto.LoanResponse;

public interface CreateLoanUseCase {
    LoanResponse createLoan(CreateLoanRequest request, Long userId, String userEmail);
}
