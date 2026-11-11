package com.makers.loansystem.application.service;

import com.makers.loansystem.application.dto.CreateLoanRequest;
import com.makers.loansystem.application.dto.LoanResponse;
import com.makers.loansystem.application.dto.RejectLoanRequest;
import com.makers.loansystem.application.port.in.LoanEventStreamUseCase;
import com.makers.loansystem.domain.exception.InvalidStatusTransitionException;
import com.makers.loansystem.domain.exception.ResourceNotFoundException;
import com.makers.loansystem.domain.exception.UnauthorizedLoanAccessException;
import com.makers.loansystem.domain.model.Loan;
import com.makers.loansystem.domain.model.LoanStatus;
import com.makers.loansystem.domain.port.LoanRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - LoanApplicationService")
class LoanApplicationServiceTest {

    @Mock
    private LoanRepositoryPort loanRepository;

    @Mock
    private LoanEventStreamUseCase eventStream;

    @Mock
    private ReactiveCreditScoreService creditScoreService;

    @InjectMocks
    private LoanApplicationService loanService;

    private Loan sampleLoan;

    @BeforeEach
    void setUp() {
        sampleLoan = Loan.request(new BigDecimal("5000.00"), 12, 10L, "usuario@test.com");
        sampleLoan.setId(100L);
    }

    @Test
    @DisplayName("createLoan: Debe persistir el préstamo, invocar scoring reactivo y emitir evento")
    void shouldCreateLoanSuccessfully() {
        CreateLoanRequest request = new CreateLoanRequest(new BigDecimal("5000.00"), 12);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan l = invocation.getArgument(0);
            l.setId(100L);
            return l;
        });
        when(creditScoreService.evaluateCreditRisk(anyLong(), any(), anyInt()))
                .thenReturn(Mono.just(new ReactiveCreditScoreService.CreditEvaluation(750, "LOW", true)));

        LoanResponse response = loanService.createLoan(request, 10L, "usuario@test.com");

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(LoanStatus.PENDING, response.status());
        assertEquals(new BigDecimal("5000.00"), response.amount());

        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(eventStream, times(1)).publishEvent(any());
        verify(creditScoreService, times(1)).evaluateCreditRisk(10L, new BigDecimal("5000.00"), 12);
    }

    @Test
    @DisplayName("approveLoan: Debe cambiar estado a APPROVED y emitir evento SSE")
    void shouldApproveLoanSuccessfully() {
        when(loanRepository.findById(100L)).thenReturn(Optional.of(sampleLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanResponse response = loanService.approveLoan(100L);

        assertEquals(LoanStatus.APPROVED, response.status());
        verify(loanRepository).save(sampleLoan);
        verify(eventStream).publishEvent(any());
    }

    @Test
    @DisplayName("approveLoan: Debe lanzar ResourceNotFoundException si el préstamo no existe")
    void shouldThrowNotFoundWhenApprovingNonExistentLoan() {
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loanService.approveLoan(999L));
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("approveLoan: Debe lanzar InvalidStatusTransitionException si ya estaba resuelto")
    void shouldThrowConflictWhenApprovingAlreadyApprovedLoan() {
        sampleLoan.approve();
        when(loanRepository.findById(100L)).thenReturn(Optional.of(sampleLoan));

        assertThrows(InvalidStatusTransitionException.class, () -> loanService.approveLoan(100L));
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("rejectLoan: Debe cambiar estado a REJECTED y registrar el motivo")
    void shouldRejectLoanSuccessfully() {
        when(loanRepository.findById(100L)).thenReturn(Optional.of(sampleLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        RejectLoanRequest request = new RejectLoanRequest("Endeudamiento alto");
        LoanResponse response = loanService.rejectLoan(100L, request);

        assertEquals(LoanStatus.REJECTED, response.status());
        assertEquals("Endeudamiento alto", response.rejectionReason());
        verify(loanRepository).save(sampleLoan);
        verify(eventStream).publishEvent(any());
    }

    @Test
    @DisplayName("getLoanById: Propietario del préstamo puede consultarlo")
    void shouldAllowOwnerToGetLoanById() {
        when(loanRepository.findById(100L)).thenReturn(Optional.of(sampleLoan));

        LoanResponse response = loanService.getLoanById(100L, 10L, false);

        assertNotNull(response);
        assertEquals(100L, response.id());
    }

    @Test
    @DisplayName("getLoanById: Administrador puede consultar préstamo de cualquier usuario")
    void shouldAllowAdminToGetAnyLoanById() {
        when(loanRepository.findById(100L)).thenReturn(Optional.of(sampleLoan));

        LoanResponse response = loanService.getLoanById(100L, 999L, true);

        assertNotNull(response);
        assertEquals(100L, response.id());
    }

    @Test
    @DisplayName("getLoanById: Usuario no propietario ni admin recibe UnauthorizedLoanAccessException")
    void shouldDenyAccessToNonOwnerNonAdmin() {
        when(loanRepository.findById(100L)).thenReturn(Optional.of(sampleLoan));

        assertThrows(UnauthorizedLoanAccessException.class, () ->
                loanService.getLoanById(100L, 999L, false));
    }

    @Test
    @DisplayName("getMyLoans: Debe retornar solo los préstamos del usuario")
    void shouldReturnUserLoans() {
        when(loanRepository.findByUserId(10L)).thenReturn(List.of(sampleLoan));

        List<LoanResponse> list = loanService.getMyLoans(10L);

        assertEquals(1, list.size());
        assertEquals(100L, list.get(0).id());
    }
}
