package com.makers.loansystem.application.port.in;

import com.makers.loansystem.application.dto.LoanResponse;

import java.util.List;

public interface GetLoansUseCase {
    LoanResponse getLoanById(Long id, Long currentUserId, boolean isAdmin);
    List<LoanResponse> getMyLoans(Long currentUserId);
    List<LoanResponse> getAllLoans();
}
